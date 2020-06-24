package com.miaxis.thermal.manager.strategy;

public enum Sign {
    XH,
    XH_N,
    XH_C,
    MR870,
    MR870A,
    ZH,
    TPS980P,
    TPS980P_C,
    MR890;

    public static String getSignName(Sign sign) {
        switch (sign) {
            case XH:
                return "XH";
            case XH_N:
                return "XH_N";
            case XH_C:
                return "XH_C";
            case MR870:
                return "MR870";
            case MR870A:
                return "MR870A";
            case ZH:
                return "ZH";
            case TPS980P:
                return "TPS980P";
            case TPS980P_C:
                return "TPS980P_C";
            case MR890:
                return "MR890";
        }
        return "error";
    }

}