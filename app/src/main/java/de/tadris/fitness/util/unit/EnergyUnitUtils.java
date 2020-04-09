package de.tadris.fitness.util.unit;

import android.content.Context;

import de.tadris.fitness.Instance;

public class EnergyUnitUtils extends UnitUtils {

    private EnergyUnit energyUnit;

    public EnergyUnitUtils(Context context) {
        super(context);
        setUnit();
    }

    public void setUnit() {
        energyUnit = getUnitById(Instance.getInstance(context).userPreferences.getEnergyUnit());
    }

    private EnergyUnit getUnitById(String id) {
        switch (id) {
            default:
            case "kcal":
                return new Kcal();
            case "joule":
                return new KJoule();
        }
    }

    public String getEnergy(double energyInKcal) {
        return getEnergy(energyInKcal, false);
    }

    public String getEnergy(double energyInKcal, boolean useLongNames) {
        int value = (int) Math.round(energyUnit.getEnergy(energyInKcal));
        if (useLongNames) {
            return value + " " + getString(energyUnit.getLongNameTitle());
        } else {
            return value + " " + energyUnit.getInternationalShortName();
        }
    }

    public String getRelativeEnergy(double energyInKcalPerMinute) {
        String value = round(energyUnit.getEnergy(energyInKcalPerMinute), 2);
        return value + " " + energyUnit.getInternationalShortName() + "/min";
    }

}
