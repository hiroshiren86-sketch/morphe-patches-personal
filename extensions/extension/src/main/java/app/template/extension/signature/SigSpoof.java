package app.template.extension.signature;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Spoof de la firma original a nivel PackageManager (tecnica "signature killer"):
 * reemplaza el IPackageManager (campo mPM de ApplicationPackageManager) por un
 * proxy que devuelve la firma ORIGINAL en getPackageInfo para el paquete propio.
 *
 * La proteccion nativa (libprotection.so) obtiene la firma via
 * ActivityThread.currentApplication().getPackageManager().getPackageInfo(...),
 * asi que con este proxy la libreria nativa ve la firma original y calcula
 * proofs validos para el servidor, aunque el APK este re-firmado.
 */
public final class SigSpoof {

    private static final String OWN_PACKAGE = "com.darkdeveloper.kawaiianimes";

    /** Certificado DER de la firma original del APK (hex). */
    private static final String ORIGINAL_CERT_HEX =
        "3082034930820231a00302010202086af5b41ff3e52f0a300d06092a864886f70d01010b05003052310b3009060355040613024d58310b3009060355040813024d58310b3009060355040713024d58310b3009060355040a13024e46310b3009060355040b13024e46310f300d060355040313064e6f466163653020170d3236303632313030333032325a180f32303533313130363030333032325a3052310b3009060355040613024d58310b3009060355040813024d58310b3009060355040713024d58310b3009060355040a13024e46310b3009060355040b13024e46310f300d060355040313064e6f4661636530820122300d06092a864886f70d01010105000382010f003082010a0282010100c464f17713910039a18cfcf8116fb2c91b75a1f43ee002624eefa8650db3d0a21087b43c08ad9d9fda3719211121d1d5a6d6398c6a424ec2ceaf5f335a78c873d164076564f74888739ae3689facbdfec8c0dfd0d82c91b92a0fefb3c260d3752702af7e181031ecc4517345c4d5e70a84cde599dd49df0567a37254550f1fb88ae5bf8952d25a7d3a1d6190a841d59a888a8e981d8093eb33506e5178b15d5401c3b15b0ae8876a096a23e0cf44ebaaa45157502c58ea52f88ff404ab4cdd3cb7ec58317cc41e516f5c426671bac2c99a16af43c02fdde382139d33ab03eee78cb16f59677169aab6b7e3dffbe4961f343c9093c3134fde8ad2e3ca8f8a65df0203010001a321301f301d0603551d0e04160414412003abb3bf3bdaf725ec4dd1321ae777fc156e300d06092a864886f70d01010b05000382010100c02fc720fa7cfdc7252b138a05b269d305dc1f24cdfa8a559f6be70435a9e646643e11e5525491e11145872522c364e776d6143a30e89f91fa9e06faebabde084e0e9f231d7ed51d2639482409268f5c83421bca3dc0c39b8fd7e82aed1ebbfe9f219a12d25a8881d21e2a9289f069d6d4095b765cf5d0611b1777921ce05632f9ed0eb2c8d40d8d085f5ffb921f22048c60f1326d2dbb3f1c213825e6c1b587414fa6a38e164381cde57b8212b54fc5a1a01549ff178a82ce6c88eed5d18c4ee87754c2c39732b960fcbdf1d5e87bf806b8ea2124dd9742c957bd7827818c526d47cc7f606d53f822d2223a3652c056c19f199ff0ae3123322eb68ec105817b";

    private static volatile boolean installed = false;
    private static volatile Signature[] originalSignatures = null;

    private SigSpoof() {
    }

    /** Idempotente. Instala el proxy en el PackageManager del contexto dado. */
    public static void install(Context context) {
        if (installed || context == null) {
            return;
        }
        synchronized (SigSpoof.class) {
            if (installed) {
                return;
            }
            try {
                PackageManager pm = context.getPackageManager();
                if (pm == null) {
                    return;
                }
                Field field = findField(pm.getClass(), "mPM");
                if (field == null) {
                    return;
                }
                field.setAccessible(true);
                Object real = field.get(pm);
                if (real == null) {
                    return;
                }
                Class<?> ipmClass = Class.forName("android.content.pm.IPackageManager");
                Object proxy = Proxy.newProxyInstance(
                    SigSpoof.class.getClassLoader(),
                    new Class<?>[]{ipmClass},
                    new PmsHandler(real));
                field.set(pm, proxy);
                installed = true;
            } catch (Throwable t) {
                // Sin el proxy la app sigue funcionando con la firma re-firmada.
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

    static void spoofPackageInfo(PackageInfo pi) {
        if (pi == null) {
            return;
        }
        try {
            pi.signatures = originalSignatures();
        } catch (Throwable t) {
            // ignorado
        }
        try {
            spoofSigningInfo(pi.signingInfo);
        } catch (Throwable t) {
            // ignorado
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
                try {
                    spoofPackageInfo((PackageInfo) result);
                } catch (Throwable t) {
                    // ignorado
                }
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
