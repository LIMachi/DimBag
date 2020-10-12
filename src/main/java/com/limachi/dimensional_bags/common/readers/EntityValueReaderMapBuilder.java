package com.limachi.dimensional_bags.common.readers;

import com.limachi.dimensional_bags.DimBag;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.function.Function;

public class EntityValueReaderMapBuilder<T extends Entity> {
    /*
    protected final Class<? extends net.minecraft.entity.Entity> clazz;
    protected final HashMap<String, EntityValueReader<T>> supplierMap;
    protected final T entity;

    public EntityValueReaderMapBuilder(T entity, Class<? extends net.minecraft.entity.Entity> clazz, HashMap<String, EntityValueReader<T>> supplierMap) {
        this.entity = entity;
        this.clazz = clazz;
        this.supplierMap = supplierMap;
    }

    public EntityValueReaderMapBuilder<T> add(String key, Function<T, ?> sup) {
        Object o = sup.apply(this.entity);
        EntityReader.ValueType vt = EntityReader.ValueType.INVALID;
        if (o != null) {
            Class<?> st = o.getClass();
            if (st.isInstance(new Integer(0)))
                vt = EntityReader.ValueType.INTEGER;
            else if (st.isInstance(new Double(0)))
                vt = EntityReader.ValueType.DOUBLE;
            else if (st.isInstance(new Boolean(false)))
                vt = EntityReader.ValueType.BOOLEAN;
            else if (st.isInstance(""))
                vt = EntityReader.ValueType.STRING;
        }
        if (vt == EntityReader.ValueType.INVALID)
            DimBag.LOGGER.error("EntityValueReaderMapBuilder for '" + this.clazz + "' got invalid supplier '" + key + "' with a returned value: " + o);
        else
            supplierMap.put(key, new EntityValueReader<T>(vt, sup, this.clazz));
        return this;
    }
     */
}
