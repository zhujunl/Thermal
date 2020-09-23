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
    TPS980P_F,
    MR890,
    MR860DZ;

    public static String getSignName(Sign sign) {
        switch (sign) {
            case XH:
                return "XH";
            case XH_N:
                return "XHN";
            case XH_C:
                return "XHC";
            case MR870:
                return "MR870";
            case MR870A:
                return "MR870A";
            case ZH:
                return "ZH";
            case TPS980P:
                return "TPS980P";
            case TPS980P_C:
                return "TPS980PC";
            case TPS980P_F:
                return "TPS980PF";
            case MR890:
                return "MR890";
            case MR860DZ:
                return "MR860DZ";
        }
        return "error";
    }

}