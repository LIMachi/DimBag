package com.limachi.dimensional_bags.common.readers;

import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Function;

public class EntityValueReader<T extends Entity> {
    /*
    private final EntityReader.ValueType valueType;
    private final Function<T, ?> supplier;
    private final Class<? extends Entity> entityClass;

    protected EntityValueReader(EntityReader.ValueType valueType, Function<T, ?> supplier, Class<? extends Entity> entityClass) {
        this.valueType = valueType;
        this.supplier = supplier;
        this.entityClass = entityClass;
    }

    public boolean matchClass(T entity) { return entityClass.isInstance(entity); }
    public EntityReader.ValueType getValueType() { return valueType; }
    public Object getValue(T entity) { return supplier.apply(entity); }

    public ITextComponent getPrintable(String key, T entity) { return new TranslationTextComponent("entity_value_reader.printable", entityClass.toString(), key, getValue(entity), valueType.name()); }
     */
}
