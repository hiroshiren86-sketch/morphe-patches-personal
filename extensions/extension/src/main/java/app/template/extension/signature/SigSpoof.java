package app.template.extension.signature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.MessageDigest;

/**
 * Spoof de la firma original a nivel PackageManager (tecnica "signature killer"):
 * reemplaza el IPackageManager (campo mPM de ApplicationPackageManager) por un
 * proxy que devuelve la firma ORIGINAL en getPackageInfo para el paquete propio.
 *
 * La proteccion nativa (libprotection.so) obtiene la firma via
 * ActivityThread.currentApplication().getPackageManager().getPackageInfo(...),
 * asi que con este proxy la libreria nativa ve la firma original y calcula
 * proofs validos para el servidor, aunque el APK este re-firmado.
 *
 * v2: logging detallado a logcat (tag SigSpoof) y a un archivo visible sin
 * ADB: /storage/emulated/0/Android/data/com.darkdeveloper.kawaiianimes/files/
 * sigspoof.log (se puede abrir con cualquier explorador de archivos).
 */
public final class SigSpoof {

    private static final String TAG = "SigSpoof";
    private static final String OWN_PACKAGE = "com.darkdeveloper.kawaiianimes";
    private static final int MAX_LOG_LINES = 400;
    private static final int MAX_INTERCEPT_LOGS = 30;

    /** Certificado DER de la firma original del APK (hex). */
    private static final String ORIGINAL_CERT_HEX =
        "3082034930820231a00302010202086af5b41ff3e52f0a300d06092a864886f70d01010b05003052310b3009060355040613024d58310b3009060355040813024d58310b3009060355040713024d58310b3009060355040a13024e46310b3009060355040b13024e46310f300d060355040313064e6f466163653020170d3236303632313030333032325a180f32303533313130363030333032325a3052310b3009060355040613024d58310b3009060355040813024d58310b3009060355040713024d58310b3009060355040a13024e46310b3009060355040b13024e46310f300d060355040313064e6f4661636530820122300d06092a864886f70d01010105000382010f003082010a0282010100c464f17713910039a18cfcf8116fb2c91b75a1f43ee002624eefa8650db3d0a21087b43c08ad9d9fda3719211121d1d5a6d6398c6a424ec2ceaf5f335a78c873d164076564f74888739ae3689facbdfec8c0dfd0d82c91b92a0fefb3c260d3752702af7e181031ecc4517345c4d5e70a84cde599dd49df0567a37254550f1fb88ae5bf8952d25a7d3a1d6190a841d59a888a8e981d8093eb33506e5178b15d5401c3b15b0ae8876a096a23e0cf44ebaaa45157502c58ea52f88ff404ab4cdd3cb7ec58317cc41e516f5c426671bac2c99a16af43c02fdde382139d33ab03eee78cb16f59677169aab6b7e3dffbe4961f343c9093c3134fde8ad2e3ca8f8a65df0203010001a321301f301d0603551d0e04160414412003abb3bf3bdaf725ec4dd1321ae777fc156e300d06092a864886f70d01010b05000382010100c02fc720fa7cfdc7252b138a05b269d305dc1f24cdfa8a559f6be70435a9e646643e11e5525491e11145872522c364e776d6143a30e89f91fa9e06faebabde084e0e9f231d7ed51d2639482409268f5c83421bca3dc0c39b8fd7e82aed1ebbfe9f219a12d25a8881d21e2a9289f069d6d4095b765cf5d0611b1777921ce05632f9ed0eb2c8d40d8d085f5ffb921f22048c60f1326d2dbb3f1c213825e6c1b587414fa6a38e164381cde57b8212b54fc5a1a01549ff178a82ce6c88eed5d18c4ee87754c2c39732b960fcbdf1d5e87bf806b8ea2124dd9742c957bd7827818c526d47cc7f606d53f822d2223a3652c056c19f199ff0ae3123322eb68ec105817b";

    private static final Object LOCK = new Object();
    private static volatile boolean installed = false;
    private static volatile Signature[] originalSignatures = null;
    private static volatile FileWriter logWriter = null;
    private static File logFile = null;
    private static int logLines = 0;
    private static int interceptLogs = 0;

    private SigSpoof() {
    }

    /**
     * Idempotente. Instala el proxy en el PackageManager del contexto dado.
     * Llamarlo desde Application.attachBaseContext para cubrir tambien a los
     * ContentProviders y a cualquier inicializacion temprana.
     */
    public static void install(Context context) {
        if (context == null) {
            log("install: context nulo");
            return;
        }
        synchronized (LOCK) {
            openLogFile(context);
            if (installed) {
                log("install: ya instalado");
                return;
            }
            try {
                PackageManager pm = context.getPackageManager();
                if (pm == null) {
                    log("install: FALLO getPackageManager() == null");
                    return;
                }
                log("install: pm=" + pm.getClass().getName());
                Field field = findField(pm.getClass(), "mPM");
                if (field == null) {
                    log("install: FALLO campo mPM no encontrado");
                    return;
                }
                field.setAccessible(true);
                Object real = field.get(pm);
                if (real == null) {
                    log("install: FALLO valor de mPM == null");
                    return;
                }
                log("install: mPM real=" + real.getClass().getName());
                Class<?> ipmClass = Class.forName("android.content.pm.IPackageManager");
                Object proxy = Proxy.newProxyInstance(
                    SigSpoof.class.getClassLoader(),
                    new Class<?>[]{ipmClass},
                    new PmsHandler(real));
                field.set(pm, proxy);
                installed = true;
                log("install: OK proxy instalado. Cert original sha256=" + sha256Hex(originalSignatures()[0].toByteArray()));
            } catch (Throwable t) {
                log("install: EXCEPCION " + t.getClass().getName() + ": " + t.getMessage());
            }
        }
    }

    public static boolean isInstalled() {
        return installed;
    }

    static Signature[] originalSignatures() {
        Signature[] result = originalSignatures;
        if (result == null) {
            byte[] cert = hexToBytes(ORIGINAL_CERT_HEX);
            result = new Signature[]{new Signature(cert)};
            originalSignatures = result;
        }
        return result;
    }

    static void spoofPackageInfo(PackageInfo pi, String pkg, String via) {
        if (pi == null) {
            return;
        }
        try {
            String before = pi.signatures != null && pi.signatures.length > 0
                ? sha256Hex(pi.signatures[0].toByteArray())
                : "sin-firma";
            pi.signatures = originalSignatures();
            spoofSigningInfo(pi.signingInfo);
            if (interceptLogs++ < MAX_INTERCEPT_LOGS) {
                log("spoof " + via + " pkg=" + pkg + " firmaAntes=" + before
                    + " -> firmaOriginal aplicada");
            }
        } catch (Throwable t) {
            log("spoof: EXCEPCION " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    /** Best effort: reemplaza las firmas dentro de SigningInfo (API 28+). */
    private static void spoofSigningInfo(SigningInfo si) {
        if (si == null) {
            return;
        }
        Object details = getFieldValue(si, "mSigningDetails");
        if (details == null) {
            return;
        }
        setFieldIfPresent(details, "mSignatures", originalSignatures());
        Object past = getFieldValue(details, "mPastSigningCertificates");
        if (past instanceof Signature[] && ((Signature[]) past).length > 0) {
            setFieldIfPresent(details, "mPastSigningCertificates", originalSignatures());
        }
    }

    private static Field findField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                // seguir subiendo
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    private static Object getFieldValue(Object obj, String name) {
        Field f = findField(obj.getClass(), name);
        if (f == null) {
            return null;
        }
        try {
            return f.get(obj);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void setFieldIfPresent(Object obj, String name, Object value) {
        Field f = findField(obj.getClass(), name);
        if (f == null) {
            return;
        }
        try {
            f.set(obj, value);
        } catch (Throwable t) {
            // ignorado
        }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(hex.charAt(2 * i), 16);
            int lo = Character.digit(hex.charAt(2 * i + 1), 16);
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (int i = 0; i < digest.length && sb.length() < 32; i++) {
                sb.append(Character.forDigit((digest[i] >> 4) & 0xF, 16));
                sb.append(Character.forDigit(digest[i] & 0xF, 16));
            }
            return sb.toString();
        } catch (Throwable t) {
            return "?";
        }
    }

    private static synchronized void log(String line) {
        Log.w(TAG, line);
        try {
            if (logWriter != null && logLines < MAX_LOG_LINES) {
                logWriter.write(line);
                logWriter.write("\n");
                logWriter.flush();
                logLines++;
            }
        } catch (Throwable t) {
            // el archivo no bloquea nada
        }
    }

    private static void openLogFile(Context context) {
        if (logWriter != null) {
            return;
        }
        try {
            File dir = context.getExternalFilesDir(null);
            if (dir == null) {
                dir = context.getFilesDir();
            }
            if (dir == null) {
                return;
            }
            logFile = new File(dir, "sigspoof.log");
            logWriter = new FileWriter(logFile, true);
        } catch (Throwable t) {
            logFile = null;
            logWriter = null;
        }
    }

    /** Handler del proxy de IPackageManager. */
    private static final class PmsHandler implements InvocationHandler {

        private final Object real;

        PmsHandler(Object real) {
            this.real = real;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (method.getDeclaringClass() == Object.class) {
                try {
                    return method.invoke(real, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
            if (args != null && args.length > 0
                    && (args[0] instanceof String)
                    && OWN_PACKAGE.equals(args[0])
                    && (name.equals("getPackageInfo") || name.equals("getPackageInfoAsUser"))) {
                Object result;
                try {
                    result = method.invoke(real, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
                spoofPackageInfo((PackageInfo) result, OWN_PACKAGE, name);
                return result;
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
