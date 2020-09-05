/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir.ac.ut.rebbeca.core.domain;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Mohammad TRB
 */
public class ReflectionUtils {

    public static List<Field> findFieldsByAnnotation(Class clazz, Class<? extends Annotation> annotation) {
        return getClassFields(clazz).stream().filter(x -> x.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    public static List<Field> getClassFields(Class clazz) {
        List<Field> result = new ArrayList<>();
        getClassFieldsRecusriveLy(clazz, result);
        return result;
    }

    private static void getClassFieldsRecusriveLy(Class clazz, List<Field> result) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class superz = clazz.getSuperclass();
        if (superz != null) {
            getClassFieldsRecusriveLy(superz, result);
        }
    }

    public static List<Method> getClassMethods(Class clazz) {
        List<Method> result = new ArrayList<>();
        getClassMethodsRecusriveLy(clazz, result);
        return result;
    }

    private static void getClassMethodsRecusriveLy(Class clazz, List<Method> result) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        Class superz = clazz.getSuperclass();
        if (superz != null) {
            getClassMethodsRecusriveLy(superz, result);
        }
    }

    public static Method findMethodByName(String methodName, Class clazz) throws NoSuchMethodException {
        for (Method meth : getClassMethods(clazz)) {
            if (meth.getName().equals(methodName)) {
                return meth;
            }
        }
        throw new NoSuchMethodException();
    }

    public static String getMethodName(Field field) {
        String fieldName = field.getName();
        String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return methodName;
    }

    public static String getSetterByGetter(String getter) {
        String setter = getter.replaceFirst("get", "set");
        return setter;
    }
}
