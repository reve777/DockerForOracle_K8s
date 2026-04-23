package com.portfolio.entity;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.portfolio.aspect.IvanSqlImage;
import com.portfolio.uitls.LogUtils;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditedEntity {

//    @NcccSqlImage(order = 80)
//    @Column(name = "MODIFY_USER", length = 20, nullable = false)
//    public String modifyUser;

    @IvanSqlImage(order = 81)
    @Column(name = "MODIFY_DATE")
    public LocalDateTime modifyDate;

    /*
     此方法透過反射機制，讀取被 @NcccSqlImage 註解標記的欄位，
     按照 order 屬性的順序，將這些欄位的值組合成一個逗號分隔的字串。
     * */
    public String toSqlImage() throws IllegalAccessException {
        StringBuilder sb = new StringBuilder();

        Map<Integer, String> sortedValueMap = getAllSqlImageAspectAnnotationSortedFields();

        for (Map.Entry<Integer, String> entry : sortedValueMap.entrySet()) {
            sb.append(entry.getValue()).append(",");
        }

        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}

        LogUtils.debug("toSqlImage : {}", sb);

        return sb.toString();
    }

    /*
     此私有方法負責收集所有被 @NcccSqlImage 註解標記的欄位，
     並根據 order 屬性進行排序，最終返回一個排序後的欄位值映射。
     * */
    private Map<Integer, String> getAllSqlImageAspectAnnotationSortedFields() throws IllegalAccessException {
        List<Field> allMemberFields = new ArrayList<>(Arrays.asList(this.getClass().getDeclaredFields()));
        Class<?> clazz = this.getClass();
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            Field[] declaredFields = superClass.getDeclaredFields();
            allMemberFields.addAll(Arrays.asList(declaredFields));
            superClass = superClass.getSuperclass();
        }

        Map<Integer, String> sortedValueMap = new TreeMap<>();
        for (Field field : allMemberFields) {
        	IvanSqlImage aspect = field.getAnnotation(IvanSqlImage.class);
            if (aspect == null) {
                continue;
            }

            int priority = aspect.order();

            boolean flag = field.canAccess(this);
            field.setAccessible(true);
            Object value = field.get(this);
            if (value == null) {
                sortedValueMap.put(priority, "null");
            } else {
                sortedValueMap.put(priority, value.toString());
            }

            field.setAccessible(flag);
        }

        return sortedValueMap;
    }

}
