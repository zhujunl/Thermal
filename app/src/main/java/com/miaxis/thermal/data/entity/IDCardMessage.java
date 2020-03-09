package com.miaxis.thermal.data.entity;

import android.graphics.Bitmap;

public class IDCardMessage {

    /* 注释说明：二代证 / 港澳台 / 外国人永久居留证 */
    /** 卡片类型 空值=二代证，J=港澳台，I=外国人永久居留证 **/
    private String cardType;
    /** 物理编号 **/
    private String cardId;
    /** 姓名 **/
    private String name;
    /** 出生日期 **/
    private String birthday;
    /** 地址 / 地址 / 空值 **/
    private String address;
    /** 身份证号码 / 身份证号 / 永久居留证号码 **/
    private String cardNumber;
    /** 签发机构 / 签发机构 / 申请受理机关代码 **/
    private String issuingAuthority;
    /** 有效期开始 **/
    private String validateStart;
    /** 有效期结束 **/
    private String validateEnd;
    /** 男/女 **/
    private String sex;
    /** 民族 / 空值 / 国籍或所在地区代码 **/
    private String nation;

    /** 指纹0 **/
    private String fingerprint0;
    /** 指纹0指位 **/
    private String fingerprintPosition0;
    /** 指纹1 **/
    private String fingerprint1;
    /** 指纹1指位 **/
    private String localeFingerprint;

    /** 港澳台：通行证号码 **/
    private String passNumber;
    /** 港澳台：签发次数 **/
    private String issueCount;
    /** 外国人：中文姓名 **/
    private String chineseName;
    /** 外国人：证件版本号 **/
    private String version;

    /** 身份证照片Bitmap **/
    private Bitmap cardBitmap;
    /** 身份证照片人脸特征 **/
    private byte[] cardFeature;

    public IDCardMessage() {
    }

    private IDCardMessage(Builder builder) {
        setCardType(builder.cardType);
        setCardId(builder.cardId);
        setName(builder.name);
        setBirthday(builder.birthday);
        setAddress(builder.address);
        setCardNumber(builder.cardNumber);
        setIssuingAuthority(builder.issuingAuthority);
        setValidateStart(builder.validateStart);
        setValidateEnd(builder.validateEnd);
        setSex(builder.sex);
        setNation(builder.nation);
        setFingerprint0(builder.fingerprint0);
        setFingerprintPosition0(builder.fingerprintPosition0);
        setFingerprint1(builder.fingerprint1);
        setLocaleFingerprint(builder.localeFingerprint);
        setPassNumber(builder.passNumber);
        setIssueCount(builder.issueCount);
        setChineseName(builder.chineseName);
        setVersion(builder.version);
        setCardBitmap(builder.cardBitmap);
        setCardFeature(builder.cardFeature);
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getIssuingAuthority() {
        return issuingAuthority;
    }

    public void setIssuingAuthority(String issuingAuthority) {
        this.issuingAuthority = issuingAuthority;
    }

    public String getValidateStart() {
        return validateStart;
    }

    public void setValidateStart(String validateStart) {
        this.validateStart = validateStart;
    }

    public String getValidateEnd() {
        return validateEnd;
    }

    public void setValidateEnd(String validateEnd) {
        this.validateEnd = validateEnd;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getFingerprint0() {
        return fingerprint0;
    }

    public void setFingerprint0(String fingerprint0) {
        this.fingerprint0 = fingerprint0;
    }

    public String getFingerprintPosition0() {
        return fingerprintPosition0;
    }

    public void setFingerprintPosition0(String fingerprintPosition0) {
        this.fingerprintPosition0 = fingerprintPosition0;
    }

    public String getFingerprint1() {
        return fingerprint1;
    }

    public void setFingerprint1(String fingerprint1) {
        this.fingerprint1 = fingerprint1;
    }

    public String getLocaleFingerprint() {
        return localeFingerprint;
    }

    public void setLocaleFingerprint(String localeFingerprint) {
        this.localeFingerprint = localeFingerprint;
    }

    public String getPassNumber() {
        return passNumber;
    }

    public void setPassNumber(String passNumber) {
        this.passNumber = passNumber;
    }

    public String getIssueCount() {
        return issueCount;
    }

    public void setIssueCount(String issueCount) {
        this.issueCount = issueCount;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Bitmap getCardBitmap() {
        return cardBitmap;
    }

    public void setCardBitmap(Bitmap cardBitmap) {
        this.cardBitmap = cardBitmap;
    }

    public byte[] getCardFeature() {
        return cardFeature;
    }

    public void setCardFeature(byte[] cardFeature) {
        this.cardFeature = cardFeature;
    }

    public static final class Builder {
        private String cardType;
        private String cardId;
        private String name;
        private String birthday;
        private String address;
        private String cardNumber;
        private String issuingAuthority;
        private String validateStart;
        private String validateEnd;
        private String sex;
        private String nation;
        private String fingerprint0;
        private String fingerprintPosition0;
        private String fingerprint1;
        private String localeFingerprint;
        private String passNumber;
        private String issueCount;
        private String chineseName;
        private String version;
        private Bitmap cardBitmap;
        private byte[] cardFeature;

        public Builder() {
        }

        public Builder cardType(String val) {
            cardType = val;
            return this;
        }

        public Builder cardId(String val) {
            cardId = val;
            return this;
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder birthday(String val) {
            birthday = val;
            return this;
        }

        public Builder address(String val) {
            address = val;
            return this;
        }

        public Builder cardNumber(String val) {
            cardNumber = val;
            return this;
        }

        public Builder issuingAuthority(String val) {
            issuingAuthority = val;
            return this;
        }

        public Builder validateStart(String val) {
            validateStart = val;
            return this;
        }

        public Builder validateEnd(String val) {
            validateEnd = val;
            return this;
        }

        public Builder sex(String val) {
            sex = val;
            return this;
        }

        public Builder nation(String val) {
            nation = val;
            return this;
        }

        public Builder fingerprint0(String val) {
            fingerprint0 = val;
            return this;
        }

        public Builder fingerprintPosition0(String val) {
            fingerprintPosition0 = val;
            return this;
        }

        public Builder fingerprint1(String val) {
            fingerprint1 = val;
            return this;
        }

        public Builder localeFingerprint(String val) {
            localeFingerprint = val;
            return this;
        }

        public Builder passNumber(String val) {
            passNumber = val;
            return this;
        }

        public Builder issueCount(String val) {
            issueCount = val;
            return this;
        }

        public Builder chineseName(String val) {
            chineseName = val;
            return this;
        }

        public Builder version(String val) {
            version = val;
            return this;
        }

        public Builder cardBitmap(Bitmap val) {
            cardBitmap = val;
            return this;
        }

        public Builder cardFeature(byte[] val) {
            cardFeature = val;
            return this;
        }

        public IDCardMessage build() {
            return new IDCardMessage(this);
        }
    }
}
