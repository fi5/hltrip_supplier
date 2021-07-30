package com.huoli.trip.supplier.web.caissa.enmu;

public enum TrafficEnum {

    AIR("air","1"),
    BUS("bus","2"),
    TRAIN("train","3"),
    SELF("self","13")




    ;

    private String name;
    private String id;

    TrafficEnum(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static String getNameByCode(String code) {
        String name = "";
        for (TrafficEnum currencyEnum : TrafficEnum.values()) {
            if (currencyEnum.id.equals(code)) {
                name = currencyEnum.getName();
            }
        }
        return name;
    }

    public static String getCodeByName(String name) {
        String code = "";
        for (TrafficEnum currencyEnum : TrafficEnum.values()) {
            if (currencyEnum.name.equals(name)) {
                code = currencyEnum.getId();
            }
        }
        return code;
    }

    public static boolean isExist(String code) {
        boolean result = false;
        for (TrafficEnum currencyEnum : TrafficEnum.values()) {
            if (currencyEnum.id.equals(code)) {
                result = true;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(getNameByCode("1.3.1"));
        boolean exist = isExist("1.4.1");
        System.out.println(exist);
        boolean exist1 = isExist("1.3.0");
        System.out.println(exist1);
    }
}
