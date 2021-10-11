/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datasourcemanager.core.formdata;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.exception.WarnException;
import com.webank.wedatasphere.linkis.datasourcemanager.common.ServiceErrorCode;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Custom Transformer of multipart form
 */
public class CustomMultiPartFormDataTransformer implements MultiPartFormDataTransformer{
    private static final Logger LOG = LoggerFactory.getLogger(CustomMultiPartFormDataTransformer.class);
    @Override
    public <T> T transformToObject(FormDataMultiPart formData, Class<?> clazz,
                                   Validator beanValidator)
        throws ValidationException, ErrorException {
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Field> objectFieldMap = new HashMap<>();
        Object object;
        try {
            object = clazz.getConstructor().newInstance();
            //Put the bean fields information into map
            for (Field field : fields) {
                objectFieldMap.put(field.getName(), field);
            }
            Map<String, List<FormDataBodyPart>> formDataParts = formData.getFields();
            formDataParts.forEach((partName, bodyParts) -> {
                List<String> stepFieldNames = Arrays.asList(StringUtils.split(partName.replace("[", ".["), "."));
                String rootFieldName = stepFieldNames.get(0);
                Field field = objectFieldMap.get(rootFieldName);
                if (null != field) {
                    try {
                        injectToObject(object, field, stepFieldNames, 0, transformBodyPartToObject(bodyParts));
                    } catch (Exception e) {
                        throw new WarnException(ServiceErrorCode.TRANSFORM_FORM_ERROR.getValue(), "Fail to inject request parameter to object, message:[" + e.getMessage() +"]");
                    }
                }
            });
        }catch(Exception e){
            LOG.error("Error in transform multipart from: " + e.getMessage());
            throw new ErrorException(ServiceErrorCode.TRANSFORM_FORM_ERROR.getValue(), "Error in transform multipart from: " + e.getMessage());
        }
        if(null != beanValidator){
            Set<ConstraintViolation<T>> result = beanValidator.validate((T)object, Default.class);
            if(result.size() > 0){
                throw new ConstraintViolationException(result);
            }
        }
        return (T)object;
    }


    /**
     * Transform body parts
     * @param bodyParts body parts
     * @return
     */
    private Object transformBodyPartToObject(List<FormDataBodyPart> bodyParts){
        if(bodyParts.size() > 0){
            Object[] result = new Object[bodyParts.size()];
            for(int i = 0 ; i < bodyParts.size(); i++){
                //Get media type
                FormDataBodyPart bodyPart = bodyParts.get(i);
                MediaType mediaType = bodyPart.getMediaType();
                if(mediaType.getType().equals(MediaType.TEXT_PLAIN_TYPE.getType())){
                    result[i] = bodyPart.getValue();
                }else {
                    //Think others as file
                    FormStreamContent streamContent = new FormStreamContent();
                    FormDataContentDisposition disposition = bodyPart.getFormDataContentDisposition();
                    streamContent.setStream(new ByteArrayInputStream(bodyPart.getValueAs(byte[].class)));
                    streamContent.setFileName(disposition.getFileName());
                    streamContent.setCreateDate(disposition.getCreationDate());
                    streamContent.setModifyDate(disposition.getModificationDate());
                    streamContent.setParameters(disposition.getParameters());
                    result[i] = streamContent;
                }
            }
            return result.length > 1 ? result : result[0];
        }
        return null;
    }

    /**
     * Inject value to object's field
     * @param object object injected
     * @param field field entity
     * @param stepFieldNames step field name list
     * @param step step index
     * @param value actual value
     * @throws Exception
     */
    private void injectToObject(Object object, Field field,
                                        List<String> stepFieldNames, int step, Object value) throws Exception {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        if(step + 1 >= stepFieldNames.size()){
            if(fieldType.equals(String.class) || PrimitiveUtils.isPrimitive(fieldType)){
                setObjectField(object, field, PrimitiveUtils.primitiveTypeConverse(value, fieldType));
            } else if (fieldType.equals(Object.class)) {
                setObjectField(object, field, value);
            } else {
                throw new IllegalAccessException("Cannot set value: " + value + " to object field: " + fieldName);
            }
        }else if(!PrimitiveUtils.isPrimitive(fieldType)){
            Object subObject = getObjectField(object, field);
            if(null == subObject){
                if(fieldType.equals(Map.class)){
                    subObject = HashMap.class.getConstructor().newInstance();
                }else if(fieldType.equals(List.class)){
                    subObject = ArrayList.class.getConstructor().newInstance();
                }else {
                    subObject = fieldType.getConstructor().newInstance();
                }
                setObjectField(object, field, subObject);
            }
            injectRecurse(subObject, field, stepFieldNames, step + 1, value);
        }

    }

    /**
     * Inject value to map structure
     * @param mapObject map
     * @param valueType value type
     * @param stepFieldNames step field name list
     * @param step step index
     * @param value actual value
     * @throws Exception
     */
    private void injectToMap(Map mapObject, Class<?> valueType,
                             List<String> stepFieldNames, int step, Object value) throws Exception{
        String fieldName = stepFieldNames.get(step);
        if(step + 1 >= stepFieldNames.size()){
            if(valueType.equals(String.class) || PrimitiveUtils.isPrimitive(valueType)){
                mapObject.put(fieldName, PrimitiveUtils.primitiveTypeConverse(value, valueType));
            }else if (valueType.equals(Object.class)){
                mapObject.put(fieldName, value);
            }else{
                throw new IllegalAccessException("Cannot set value: " + value + " to map: " + stepFieldNames.get(step - 1));
            }
        }
    }

    /**
     * Inject value to list structure
     * @param listObject list object
     * @param elementType element type
     * @param stepFieldNames step field name list
     * @param step step index
     * @param value value
     * @throws Exception
     */
    private void injectToList(List listObject, Class<?> elementType,
                              List<String> stepFieldNames, int step, Object value) throws Exception{
        String fieldName = stepFieldNames.get(step);
        if(step + 1 >= stepFieldNames.size() && fieldName.matches("\\[\\d+]")){
            int index = Integer.parseInt(fieldName.substring(1, fieldName.length() - 1));
            //expand list
            int expand = index + 1 - listObject.size();
            while (expand-- > 0) {
                listObject.add(null);
            }
            if(elementType.equals(String.class) || PrimitiveUtils.isPrimitive(elementType)){
                listObject.set(index, PrimitiveUtils.primitiveTypeConverse(value, elementType));
            }else if (elementType.equals(Object.class)){
                listObject.set(index, value);
            }else{
                throw new IllegalAccessException("Cannot set value: " + value + " to array: " + stepFieldNames.get(step - 1));
            }
        }
    }

    /**
     * Inject recursively
     * @param subObject sub object
     * @param field sub object's field
     * @param stepFieldNames step field name list
     * @param step step index
     * @param value actual value
     * @throws Exception
     */
    private void injectRecurse(Object subObject, Field field,
                               List<String> stepFieldNames, int step, Object value) throws Exception{
        Class<?> fieldType = field.getType();
        if(fieldType.equals(Map.class)){
            Class<?>[] generic = getGenericTypes(field);
            if(null == generic || generic[0].equals(String.class)){
                Class<?> valueType = null == generic ? String.class : generic[1];
                injectToMap((Map) subObject, valueType,  stepFieldNames, step, value);
            }
        }else if(fieldType.equals(List.class)){
            Class<?>[] generic = getGenericTypes(field);
            injectToList((List)subObject, generic == null ? String.class : generic[0], stepFieldNames,
                    step, value );
        }else{
            String nextFieldName = stepFieldNames.get(step);
            Field nextField = subObject.getClass().getField(nextFieldName);
            injectToObject(subObject, nextField, stepFieldNames, step, value);
        }
    }

    /**
     * Get generic types
     * @param field
     * @return
     */
    private Class<?>[] getGenericTypes(Field field){
        Type fc = field.getGenericType();
        if(fc instanceof ParameterizedType){
            Type[] types = ((ParameterizedType)fc).getActualTypeArguments();
            if(null != types && types.length > 0){
                Class<?>[] genericClazz = new Class<?>[types.length];
                for(int i = 0 ; i < genericClazz.length ; i++){
                    genericClazz[i] = (Class<?>)types[i];
                }
                return genericClazz;
            }
        }
        return null;
    }

    private void setObjectField(Object object, Field field, Object value) throws Exception{
        field.setAccessible(true);
        field.set(object, value);
    }

    private Object getObjectField(Object object, Field field) throws Exception{
        field.setAccessible(true);
        return field.get(object);
    }


    /**
     * Tool of primitive
     */
    public static class PrimitiveUtils{
        public static Object primitiveTypeConverse(Object objValue, Class<?> type){
            if(type.equals(String.class) || null == objValue){
                return objValue;
            }
            String value = String.valueOf(objValue);
            if(!type.isPrimitive()){
                try {
                    type = ((Class)type.getField("TYPE").get(null));
                } catch (Exception e) {
                    //ignore
                }
            }
            switch (type.getSimpleName()) {
                case "int":
                    return Integer.valueOf(value);
                case "long":
                    return Long.valueOf(value);
                case "short":
                    return Short.valueOf(value);
                case "char":
                    return value.toCharArray()[0];
                case "float":
                    return Float.valueOf(value);
                case "double":
                    return Double.valueOf(value);
                case "byte":
                    return Byte.valueOf(value);
                case "boolean":
                    return Boolean.valueOf(value);
                default:
                    throw new RuntimeException("Type: " + type.getSimpleName() + " is not primitive");
            }
        }

        public static boolean isPrimitive(Class<?> type){
            try {
                return type.isPrimitive() || ((Class) type.getField("TYPE").get(null)).isPrimitive();
            }catch(Exception e){
                return false;
            }
        }
    }

}
