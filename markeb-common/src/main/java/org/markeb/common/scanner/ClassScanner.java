package org.markeb.common.scanner;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * ClassScanner 支持链式条件扫描：
 * - 扫描包下指定父类/接口
 * - 扫描包下指定注解
 * - 扫描指定类
 * - 可选仅返回 Spring 管理的 Bean
 */
public class ClassScanner {

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    private final SimpleMetadataReaderFactory factory = new SimpleMetadataReaderFactory();

    private ClassScanner() {
    }

    public static ScanBuilder builder() {
        return new ScanBuilder(new ClassScanner());
    }

    public static class ScanBuilder {
        private final ClassScanner scanner;
        private final Set<TypeFilter> filters = new HashSet<>();
        private String[] basePackages = new String[0];
        private boolean onlySpringBeans = false;
        private ApplicationContext applicationContext;

        private ScanBuilder(ClassScanner scanner) {
            this.scanner = scanner;
        }

        /**
         * 设置扫描包
         */
        public ScanBuilder basePackages(String... basePackages) {
            this.basePackages = basePackages;
            return this;
        }

        /**
         * 扫描实现指定接口或继承指定父类的类
         */
        public ScanBuilder bySuperType(Class<?> superType) {
            this.filters.add(new AssignableTypeFilter(superType));
            return this;
        }

        /**
         * 扫描带指定注解的类
         */
        public ScanBuilder byAnnotation(Class<? extends Annotation> annotationClass) {
            this.filters.add(new AnnotationTypeFilter(annotationClass));
            return this;
        }

        /**
         * 扫描指定类
         */
        public ScanBuilder byClasses(Class<?>... classes) {
            for (Class<?> clazz : classes) {
                this.filters.add(new SpecificClassFilter(clazz));
            }
            return this;
        }

        /**
         * 是否仅返回 Spring 管理的 Bean
         */
        public ScanBuilder onlySpringBeans(ApplicationContext context) {
            this.onlySpringBeans = true;
            this.applicationContext = context;
            return this;
        }

        /**
         * 执行扫描
         */
        public Set<Class<?>> scan() {
            Set<Class<?>> result = new HashSet<>();
            for (String pkg : basePackages) {
                String pattern = "classpath*:" + pkg.replace('.', '/') + "/**/*.class";
                try {
                    Resource[] resources = scanner.resolver.getResources(pattern);
                    for (Resource resource : resources) {
                        if (!resource.isReadable()) continue;
                        MetadataReader reader = scanner.factory.getMetadataReader(resource);

                        boolean match = filters.isEmpty();
                        for (TypeFilter filter : filters) {
                            if (filter.match(reader, scanner.factory)) {
                                match = true;
                                break;
                            }
                        }

                        if (match) {
                            Class<?> clazz = Class.forName(reader.getClassMetadata().getClassName());
                            if (!onlySpringBeans || isSpringBean(clazz)) {
                                result.add(clazz);
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("Failed to scan package: " + pkg, e);
                }
            }
            return result;
        }

        private boolean isSpringBean(Class<?> clazz) {
            if (applicationContext == null) return false;
            return !applicationContext.getBeansOfType(clazz).isEmpty();
        }
    }

    /**
     * 内部专用 TypeFilter，用于指定类
     */
    private static class SpecificClassFilter implements TypeFilter {
        private final Class<?> target;

        public SpecificClassFilter(Class<?> target) {
            this.target = target;
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
            return metadataReader.getClassMetadata().getClassName().equals(target.getName());
        }
    }

    public static MethodHandle bindBean(Object bean, Method method) {
        method.setAccessible(true);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh;
        try {
            mh = lookup.unreflect(method).bindTo(bean);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return mh;
    }

}
