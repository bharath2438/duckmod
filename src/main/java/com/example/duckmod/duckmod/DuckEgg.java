package com.example.duckmod.duckmod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

public class DuckEgg extends Item {

    private int counter = 2500;
    public DuckEgg(FabricItemSettings settings) {
        super(settings);
    }

    public void setCounter(int value) {
        this.counter = value;
    }
}
