package com.miaxis.thermal.manager.strategy.mr870a;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.miaxis.thermal.data.entity.IDCardMessage;
import com.miaxis.thermal.manager.CardManager;
import com.sdt.Sdtapi;
import com.zkteco.android.IDReader.WLTService;

import java.io.ByteArrayOutputStream;

public class MR870ACardStrategy implements CardManager.CardStrategy {

    private static final String[] NATION = {"汉", "蒙古", "回", "藏", "维吾尔", "苗", "彝", "壮", "布依", "朝鲜",
            "满", "侗", "瑶", "白", "土家", "哈尼", "哈萨克", "傣", "黎", "傈僳",
            "佤", "畲", "高山", "拉祜", "水", "东乡", "纳西", "景颇", "克尔克孜", "土",
            "达斡尔", "仫佬", "羌", "布朗", "撒拉", "毛南", "仡佬", "锡伯", "阿昌", "普米",
            "塔吉克", "怒", "乌兹别克", "俄罗斯", "鄂温克", "德昂", "保安", "裕固", "京", "塔塔尔",
            "独龙", "鄂伦春", "赫哲", "门巴", "珞巴", "基诺", "", "", "穿青人"
    };

    private Sdtapi sdtapi;
    private CardManager.OnCardStatusListener statusListener;
    private CardManager.OnCardReadListener cardListener;

    private volatile boolean sdtReInit = false;
    private volatile boolean running = false;
    private volatile boolean needReadCard = true;

    @Override
    public void initDevice(Context context, CardManager.OnCardStatusListener listener) {
        try {
            sdtapi = new Sdtapi(context);
            listener.onCardStatus(true);
        } catch (Exception e) {
            e.printStackTrace();
            sdtReInit = false;
        }
    }

    @Override
    public void startReadCard(CardManager.OnCardReadListener listener) {
        this.cardListener = listener;
        if (!running) {
            running = true;
            needReadCard = true;
            new Thread(new ReadCardThread()).start();
        }
    }

    @Override
    public void release() {
        if (sdtapi != null) {
            running = false;
            needReadCard = false;
            sdtapi = null;
        }
    }

    @Override
    public void needNextRead(boolean need) {
        needReadCard = need;
    }

    private String readSamId() {
        if (sdtapi != null) {
            char[] chars = new char[16];
            int i = sdtapi.SDT_GetSAMIDToStr(chars);
            if (i == 0x90) {
                return String.valueOf(chars);
            }
        }
        return "";
    }

    private class ReadCardThread extends Thread {

        @Override
        public void run() {
            while (running) {
                if (sdtapi != null) {
                    if (needReadCard) {
                        byte[] message = new byte[100];
                        try {
                            int ret = sdtapi.SDT_StartFindIDCard();
                            if (ret == 0x9f) {
                                ret = sdtapi.SDT_SelectIDCard();
                                if (ret == 0x90) {
                                    IDCardMessage idCardMessage = readCardMsg();
                                    if (cardListener != null) {
                                        needReadCard = false;
                                        cardListener.onCardRead(idCardMessage);
                                    }
                                }
                            } else if (ret == 522) {
                                sdtapi = null;
                                sdtReInit = false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("asd", "读卡异常" + new String(message));
                        }
                    }
                } else {
                    if (!sdtReInit) {
                        sdtReInit = true;
                        if (statusListener != null) {
                            statusListener.onCardStatus(false);
                        }
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //读取身份证中的文字信息（可阅读格式的）
    private IDCardMessage readCardMsg() {
        int ret;
        int[] puiCHMsgLen = new int[1];
        int[] puiPHMsgLen = new int[1];
        int[] puiFPMsgLen = new int[1];

        byte[] pucCHMsg = new byte[256];
        byte[] pucPHMsg = new byte[1024];
        byte[] pucFPMsg = new byte[1024];

        //sdtapi中标准接口，输出字节格式的信息。
        ret = sdtapi.SDT_ReadBaseFPMsg(pucCHMsg, puiCHMsgLen, pucPHMsg, puiPHMsgLen, pucFPMsg, puiFPMsgLen);
        if (ret == 0x90) {
            try {
                if (puiCHMsgLen[0] == pucCHMsg.length) {
                    String type = isGreenCard(pucCHMsg);
                    IDCardMessage idCardMessage;
                    if ("I".equals(type)) {
                        idCardMessage = analysisGreenCard(pucCHMsg);
                    } else if ("J".equals(type)) {
                        idCardMessage = analysisGATCardInfo(pucCHMsg);
                    } else {
                        idCardMessage = analysisIdCardInfo(pucCHMsg);
                    }
                    if (puiPHMsgLen[0] == pucPHMsg.length) {
                        parsePicture(pucPHMsg, idCardMessage);
                    }
                    if (puiFPMsgLen[0] == pucFPMsg.length) {
                        parseFinger(pucFPMsg, idCardMessage);
                    }
                    return idCardMessage;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void parsePicture(byte[] pucPHMsg, IDCardMessage idCardMessage) {
        Bitmap bitmap = getBitmap(pucPHMsg);
        idCardMessage.setCardBitmap(bitmap);
    }

    private void parseFinger(byte[] pucFPMsg, IDCardMessage idCardMessage) {
        byte[] bFingerData0 = new byte[512];
        byte[] bFingerData1 = new byte[512];
        System.arraycopy(pucFPMsg, 0, bFingerData0, 0, bFingerData0.length);
        System.arraycopy(pucFPMsg, 512, bFingerData1, 0, bFingerData1.length);
        idCardMessage.setFingerprint0(Base64.encodeToString(bFingerData0, Base64.NO_WRAP));
        idCardMessage.setFingerprintPosition0(fingerPositionCovert(bFingerData0[5]));
        idCardMessage.setFingerprint1(Base64.encodeToString(bFingerData1, Base64.NO_WRAP));
        idCardMessage.setFingerprintPosition1(fingerPositionCovert(bFingerData1[5]));
    }

    private String isGreenCard(byte[] bCardInfo) {
        byte[] id_isGreen = new byte[2];
        id_isGreen[0] = bCardInfo[248];
        id_isGreen[1] = bCardInfo[249];
        return unicode2String(id_isGreen).trim();
    }

    private Bitmap getBitmap(byte[] wlt) {
        byte[] buffer = new byte[38556];
        int result = WLTService.wlt2Bmp(wlt, buffer);
        if (result == 1) {
            return bgr2Bitmap(buffer);
        }
        return null;
    }

    private Bitmap bgr2Bitmap(byte[] bgrbuf) {
        int width = WLTService.imgWidth;
        int height = WLTService.imgHeight;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        int row = 0, col = width - 1;
        for (int i = bgrbuf.length - 1; i >= 3; i -= 3) {
            int color = bgrbuf[i] & 0xFF;
            color += (bgrbuf[i - 1] << 8) & 0xFF00;
            color += ((bgrbuf[i - 2]) << 16) & 0xFF0000;
            bmp.setPixel(col--, row, color);
            if (col < 0) {
                col = width - 1;
                row++;
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bmp.getByteCount());
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] data = outputStream.toByteArray();
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /* 解析身份证信息 */
    private IDCardMessage analysisIdCardInfo(byte[] bCardInfo) {
        IDCardMessage idCardMessage = new IDCardMessage();
        byte[] id_Name = new byte[30]; // 姓名
        byte[] id_Sex = new byte[2]; // 性别 1为男 其他为女
        byte[] id_Rev = new byte[4]; // 民族
        byte[] id_Born = new byte[16]; // 出生日期
        byte[] id_Home = new byte[70]; // 住址
        byte[] id_Code = new byte[36]; // 身份证号
        byte[] _RegOrg = new byte[30]; // 签发机关
        byte[] id_ValidPeriodStart = new byte[16]; // 有效日期 起始日期16byte 截止日期16byte
        byte[] id_ValidPeriodEnd = new byte[16];
        byte[] id_NewAddr = new byte[36]; // 预留区域
        int iLen = 0;
        idCardMessage.setCardType("");
        System.arraycopy(bCardInfo, iLen, id_Name, 0, id_Name.length);
        iLen = iLen + id_Name.length;
        idCardMessage.setName(unicode2String(id_Name).trim());

        System.arraycopy(bCardInfo, iLen, id_Sex, 0, id_Sex.length);
        iLen = iLen + id_Sex.length;

        if (id_Sex[0] == '1') {
            idCardMessage.setSex("男");
        } else {
            idCardMessage.setSex("女");
        }

        System.arraycopy(bCardInfo, iLen, id_Rev, 0, id_Rev.length);
        iLen = iLen + id_Rev.length;
        int iRev = Integer.parseInt(unicode2String(id_Rev));
        idCardMessage.setNation(NATION[iRev - 1]);

        System.arraycopy(bCardInfo, iLen, id_Born, 0, id_Born.length);
        iLen = iLen + id_Born.length;
        idCardMessage.setBirthday(unicode2String(id_Born));

        System.arraycopy(bCardInfo, iLen, id_Home, 0, id_Home.length);
        iLen = iLen + id_Home.length;
        idCardMessage.setAddress(unicode2String(id_Home).trim());

        System.arraycopy(bCardInfo, iLen, id_Code, 0, id_Code.length);
        iLen = iLen + id_Code.length;
        idCardMessage.setCardNumber(unicode2String(id_Code).trim());

        System.arraycopy(bCardInfo, iLen, _RegOrg, 0, _RegOrg.length);
        iLen = iLen + _RegOrg.length;
        idCardMessage.setIssuingAuthority(unicode2String(_RegOrg).trim());

        System.arraycopy(bCardInfo, iLen, id_ValidPeriodStart, 0, id_ValidPeriodStart.length);
        iLen = iLen + id_ValidPeriodStart.length;
        System.arraycopy(bCardInfo, iLen, id_ValidPeriodEnd, 0, id_ValidPeriodEnd.length);
        iLen = iLen + id_ValidPeriodEnd.length;
        String validateStart = unicode2String(id_ValidPeriodStart).trim();
        String validateEnd = unicode2String(id_ValidPeriodEnd).trim();
        idCardMessage.setValidateStart(validateStart);
        idCardMessage.setValidateEnd(validateEnd);

        System.arraycopy(bCardInfo, iLen, id_NewAddr, 0, id_NewAddr.length);
        iLen = iLen + id_NewAddr.length;

        return idCardMessage;
    }

    /* 解析港澳台通行证信息 */
    public IDCardMessage analysisGATCardInfo(byte[] bCardInfo) {
        IDCardMessage idCardMessage = new IDCardMessage();
        byte[] id_Name = new byte[30]; // 姓名
        byte[] id_Sex = new byte[2]; // 性别 1为男 其他为女
        byte[] id_Rev = new byte[4]; // 预留区
        byte[] id_Born = new byte[16]; // 出生日期
        byte[] id_Home = new byte[70]; // 住址
        byte[] id_Code = new byte[36]; // 身份证号
        byte[] id_RegOrg = new byte[30]; // 签发机关
        byte[] id_ValidPeriodStart = new byte[16]; // 有效日期 起始日期16byte 截止日期16byte
        byte[] id_ValidPeriodEnd = new byte[16];
//        byte[] id_NewAddr = new byte[36]; // 预留区域
        byte[] id_PassNum = new byte[18]; //通行证号码
        byte[] id_IssueNum = new byte[4]; //签发次数
        byte[] id_NewAddr = new byte[14]; //
        int iLen = 0;
        idCardMessage.setCardType("J");

        System.arraycopy(bCardInfo, iLen, id_Name, 0, id_Name.length);
        iLen = iLen + id_Name.length;
        idCardMessage.setName(unicode2String(id_Name).trim());

        System.arraycopy(bCardInfo, iLen, id_Sex, 0, id_Sex.length);
        iLen = iLen + id_Sex.length;
        if (id_Sex[0] == '1') {
            idCardMessage.setSex("男");
        } else {
            idCardMessage.setSex("女");
        }

        System.arraycopy(bCardInfo, iLen, id_Rev, 0, id_Rev.length);
        iLen = iLen + id_Rev.length;
        idCardMessage.setNation("");

        System.arraycopy(bCardInfo, iLen, id_Born, 0, id_Born.length);
        iLen = iLen + id_Born.length;
        idCardMessage.setBirthday(unicode2String(id_Born));

        System.arraycopy(bCardInfo, iLen, id_Home, 0, id_Home.length);
        iLen = iLen + id_Home.length;
        idCardMessage.setAddress(unicode2String(id_Home).trim());

        System.arraycopy(bCardInfo, iLen, id_Code, 0, id_Code.length);
        iLen = iLen + id_Code.length;
        idCardMessage.setCardNumber(unicode2String(id_Code).trim());

        System.arraycopy(bCardInfo, iLen, id_RegOrg, 0, id_RegOrg.length);
        iLen = iLen + id_RegOrg.length;
        idCardMessage.setIssuingAuthority(unicode2String(id_RegOrg).trim());

        System.arraycopy(bCardInfo, iLen, id_ValidPeriodStart, 0, id_ValidPeriodStart.length);
        iLen = iLen + id_ValidPeriodStart.length;
        System.arraycopy(bCardInfo, iLen, id_ValidPeriodEnd, 0, id_ValidPeriodEnd.length);
        iLen = iLen + id_ValidPeriodEnd.length;
        String validateStart = unicode2String(id_ValidPeriodStart).trim();
        String validateEnd = unicode2String(id_ValidPeriodEnd).trim();
        idCardMessage.setValidateStart(validateStart);
        idCardMessage.setValidateEnd(validateEnd);

        System.arraycopy(bCardInfo, iLen, id_PassNum, 0, id_PassNum.length);
        iLen = iLen + id_PassNum.length;
        idCardMessage.setPassNumber(unicode2String(id_PassNum).trim());

        System.arraycopy(bCardInfo, iLen, id_IssueNum, 0, id_IssueNum.length);
        iLen = iLen + id_IssueNum.length;
        idCardMessage.setIssueCount(unicode2String(id_IssueNum).trim());

        System.arraycopy(bCardInfo, iLen, id_NewAddr, 0, id_NewAddr.length);
        iLen = iLen + id_NewAddr.length;

        return idCardMessage;
    }

    /* 解析外国人永久居留证信息 */
    public IDCardMessage analysisGreenCard(byte[] bCardInfo) {
        IDCardMessage idCardMessage = new IDCardMessage();
        byte[] id_Name = new byte[120];    // 姓名
        byte[] id_Sex = new byte[2];      // 性别 1为男 其他为女
        byte[] id_cardNo = new byte[30];     // 永久居留证号码
        byte[] id_nation = new byte[6];      // 国籍或所在地区代码
        byte[] id_chinese_name = new byte[30];     // 中文姓名
        byte[] id_start_date = new byte[16];     // 证件签发日期
        byte[] id_end_date = new byte[16];     // 证件终止日期
        byte[] id_birthday = new byte[16];     // 出生日期
        byte[] id_version = new byte[4];      // 证件版本号
        byte[] id_reg_org = new byte[8];      // 当前申请受理机关代码
        byte[] id_type = new byte[2];      // 证件类型标识
        byte[] id_remark = new byte[6];      // 预留项
        int iLen = 0;
        idCardMessage.setCardType("I");

        System.arraycopy(bCardInfo, iLen, id_Name, 0, id_Name.length);
        iLen = iLen + id_Name.length;
        idCardMessage.setName(unicode2String(id_Name));

        System.arraycopy(bCardInfo, iLen, id_Sex, 0, id_Sex.length);
        iLen = iLen + id_Sex.length;
        if (id_Sex[0] == '1') {
            idCardMessage.setSex("男");
        } else {
            idCardMessage.setSex("女");
        }

        System.arraycopy(bCardInfo, iLen, id_cardNo, 0, id_cardNo.length);
        iLen += id_cardNo.length;
        idCardMessage.setCardNumber(unicode2String(id_cardNo));

        System.arraycopy(bCardInfo, iLen, id_nation, 0, id_nation.length);
        iLen += id_nation.length;
        idCardMessage.setNation(unicode2String(id_nation));

        System.arraycopy(bCardInfo, iLen, id_chinese_name, 0, id_chinese_name.length);
        iLen = iLen + id_chinese_name.length;
        idCardMessage.setChineseName(unicode2String(id_chinese_name));

        System.arraycopy(bCardInfo, iLen, id_start_date, 0, id_start_date.length);
        iLen = iLen + id_start_date.length;
        System.arraycopy(bCardInfo, iLen, id_end_date, 0, id_end_date.length);
        iLen = iLen + id_end_date.length;
        String validateStart = unicode2String(id_start_date).trim();
        String validateEnd = unicode2String(id_end_date).trim();
        idCardMessage.setValidateStart(validateStart);
        idCardMessage.setValidateEnd(validateEnd);

        System.arraycopy(bCardInfo, iLen, id_birthday, 0, id_birthday.length);
        iLen = iLen + id_birthday.length;
        idCardMessage.setBirthday(unicode2String(id_birthday));

        System.arraycopy(bCardInfo, iLen, id_version, 0, id_version.length);
        iLen = iLen + id_version.length;
        idCardMessage.setVersion(unicode2String(id_version));

        System.arraycopy(bCardInfo, iLen, id_reg_org, 0, id_reg_org.length);
        iLen += id_reg_org.length;
        idCardMessage.setIssuingAuthority(unicode2String(id_reg_org));

        System.arraycopy(bCardInfo, iLen, id_type, 0, id_type.length);
        iLen += id_type.length;
        idCardMessage.setVersion(unicode2String(id_type));

        System.arraycopy(bCardInfo, iLen, id_remark, 0, id_remark.length);
        iLen += id_remark.length;

        return idCardMessage;
    }

    private static String unicode2String(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length / 2; i++) {
            int a = bytes[2 * i + 1];
            if (a < 0) {
                a = a + 256;
            }
            int b = bytes[2 * i];
            if (b < 0) {
                b = b + 256;
            }
            int c = (a << 8) | b;
            sb.append((char) c);
        }
        return sb.toString();
    }

    private static String fingerPositionCovert(byte finger) {
        switch ((int) finger) {
            case 11:
                return "右手拇指";
            case 12:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "其他不确定指位";
        }
    }

}
