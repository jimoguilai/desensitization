/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package red.zyc.desensitization.resolver;

import red.zyc.desensitization.annotation.CascadeSensitive;
import red.zyc.desensitization.support.InstanceCreators;
import red.zyc.desensitization.util.ReflectionUtil;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;

/**
 * 级联对象解析器，只会处理被<b>直接</b>标注{@link CascadeSensitive}注解、非{@code final}、非{@code null}的对象。
 *
 * @author zyc
 * @see CascadeSensitive
 */
public class CascadeTypeResolver implements TypeResolver<Object, AnnotatedType> {

    @Override
    public Object resolve(Object value, AnnotatedType annotatedType) {
        Class<?> clazz = value.getClass();
        Object newObject = InstanceCreators.getInstanceCreator(clazz).create();
        ReflectionUtil.listAllFields(clazz).parallelStream().forEach(field -> {
            Object fieldValue;
            if (!Modifier.isFinal(field.getModifiers()) && (fieldValue = ReflectionUtil.getFieldValue(value, field)) != null) {
                ReflectionUtil.setFieldValue(newObject, field, TypeResolvers.resolve(fieldValue, field.getAnnotatedType()));
            }
        });
        return newObject;
    }

    @Override
    public boolean support(Object value, AnnotatedType annotatedType) {
        return value != null && annotatedType.getDeclaredAnnotation(CascadeSensitive.class) != null;
    }

    @Override
    public int order() {
        return LOWEST_PRIORITY;
    }
}
