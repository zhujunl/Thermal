
package org.zz.jni;

public class JustouchFaceApi {

    //加载动态库
    static {
        System.loadLibrary("JustouchFaceAPI");
    }

    /****************************************************************************
     功  能：	获取算法版本
     参  数：
     返	回：	算法版本
     *****************************************************************************/
    public native String getAlgVersion();

    /****************************************************************************
     功  能：初始化算法
     参  数：context       - 输入，上下文
            szModelPath    - 输入，模型文件路径
            szLicense      - 输入，授权码
     返  回：0-成功，其他-失败
    *****************************************************************************/
    public native int initAlg(Object context,String path,String license);

    /****************************************************************************
     功  能：初始化算法（1:N）
     参  数：context       - 输入，上下文
            szModelPath    - 输入，模型文件路径
            szLicense      - 输入，授权码
            iMaxSearchNum  - 输入，最大搜索库容
     返  回：0-成功，其他-失败
    *****************************************************************************/
    public native int initAlgN(Object context,String path,String license,int iMaxSearchNum);

    /****************************************************************************
     功  能：释放算法
     参  数：
     返	回：0-成功，其他-失败
     *****************************************************************************/
    public native int freeAlg();

    /****************************************************************************
     功  能：	特征长度
     参  数：
     返	回：	特征长度
     *****************************************************************************/
    public native int getFeatureSize();

    /*******************************************************************************************
     功	能：	检测人脸
     参	数：
            pImage      - 输入，RGB图像数据
            nImgWidth   - 输入，图像宽度
            nImgHeight  - 输入，图像高度
            pFaceNum    - 输出，人脸数
            pFaceInfo   - 输出，人脸检测结果,内存分配大小 new int[262*100],结构详见MXFaceInfoEx
     返	回：	0-成功，其他-失败
     备  注：    最多检测100人
     *******************************************************************************************/
    public native int detectFace(byte[] pImage, int nWidth, int nHeight,
                                    int[] pFaceNum, int[] pFaceInfo);

    /*******************************************************************************************
     功	能：	提取人脸特征
     参	数：
                pImage      - 输入，RGB图像数据
                nImgWidth   - 输入，图像宽度
                nImgHeight  - 输入，图像高度
                pFaceNum    - 输入，人脸个数
                pFaceInfo	- 输入，人脸信息
                pFaceFea    - 输出，人脸特征
     返	回：	0-成功，其他-失败
     *******************************************************************************************/
    public native int featureExtract(byte[] pImage, int nWidth, int nHeight,
                                      int pFaceNum, int[] pFaceInfo, byte[] pFaceFea);

    /*******************************************************************************************
     功	能：比对人脸特征
     参	数：
            pFaceFeaA - 输入，人脸特征A
            pFaceFeaB - 输入，人脸特征B
            fScore    - 输出，相似性度量值，0~1.0 ，越大越相似。
     返	回：0-成功，其他-失败
     *******************************************************************************************/
    public native int featureMatch(byte[] pFaceFeaA,byte[] pFaceFeaB, float[] fScore);

    /*******************************************************************************************
     功	能：	检测人脸(跟踪人脸)，用于动态视频检测
     参	数：
            pImage      - 输入，RGB图像数据
            nImgWidth   - 输入，图像宽度
            nImgHeight  - 输入，图像高度
            pFaceNum    - 输入/输出，人脸数，须申请为全局变量，内存分配大小 new int[100]
            pFaceInfo   - 输入/输出，人脸检测结果，须申请为全局变量，内存分配大小 new int[262*100],结构详见MXFaceInfoEx
     返	回：	0-成功，其他-失败
     *******************************************************************************************/
    public native int trackFace(byte[] pImage, int nWidth, int nHeight,
                                   int[] pFaceNum, int[] pFaceInfo);

    /*******************************************************************************************
     功	能：	根据人脸框检测关键点对人脸进行质量评价
     参	数：
                pImage        - 输入，RGB图像数据
                nImgWidth     - 输入，图像宽度
                nImgHeight    - 输入，图像高度
                pFaceNum      - 输入，人脸数
                pFaceInfo	  - 输入/输出，人脸检测数据
     返	回：	0-成功，其他-失败
     *******************************************************************************************/
    public native int faceQuality(byte[] pImage, int nWidth, int nHeight,
                                     int pFaceNum, int[] pFaceInfo);

    /*******************************************************************************************
     功	能：	计算注册人脸质量分数（包含检测人脸功能）
     参	数：	pImage      - 输入，RGB图像数据
                nImgWidth   - 输入，图像宽度
                nImgHeight  - 输入，图像高度
                pFaceInfo   - 输入/输出，人脸检测数据
     返	回：	质量分(0-100),建议:90
     备 注:      只能是一个人脸
     *******************************************************************************************/
    public native int faceQuality4Reg(byte[] pImage, int nWidth, int nHeight,
                                         int[] pFaceInfo);

    /*******************************************************************************************
     功	能：	根据输入人脸特征与人脸模板集合，查找匹配人脸特征的序号
     参	数：
                pFaceFeaList - 输入，人脸模板集合(模板1+模板2+...+模板N)
                iFaceNum	 - 输入，人脸模板个数（建议小于5000）
                score        - 输入，比对分通过阈值（建议76）
                pFaceFea     - 输入，人脸特征
                pFaceInfo    - 输入/输出，人脸识别注册库id
     返	回：	0-成功，-1-识别中，其他-失败
     *******************************************************************************************/
    public native int searchFeature(byte[] pFaceFeaList, int iPictureNum, int score, byte[] pFaceFea, int[] pFaceInfo);

    /*******************************************************************************************
    功	能：	根据输入人脸特征与人脸模板集合，查找匹配人脸特征的序号(同一人可含有多张图片)
    参	数：
        faceFeaList  - 输入，人脸模板集合(模板1+模板2+...+模板N)
        pictureNum   - 输入，库中图片总数
        faceNumList  - 输入，顺序记录模板库中每个人的模板图片数(建议<=10)
        personNum    - 输入，库中总人数(ps:模板库中一个人可能有多张模板图片，所以总人数小于或等于图片总数)
        score         - 输入，比对分通过阈值（建议76）
        faceFea      - 输入，人脸特征
        faceInfo     - 输入/输出，人脸识别注册库id
    返	回：	0-成功，-1-识别中，-2-陌生人，-3人脸模板个数过多，其他失败
    *******************************************************************************************/
    public native int searchNFeature(byte[] pFaceFeaList, int iPictureNum, int[] pFaceNumList,
                                        int iPersonNum, int score, byte[] pFaceFea, int[] pFaceInfo);

    /*******************************************************************************************
     功	能：	根据输入人脸信息与人脸模板集合，查找匹配人脸特征的序号(同一人可含有多张图片)
     参	数：	pAlgEngine	  - 输入，引擎
                 pFaceFeaList  - 输入，人脸模板集合(模板1+模板2+...+模板N)
                 iPicNum       - 输入，库中图片总数
                 pFaceNumList  - 输入，顺序记录模板库中每个人的模板图片数(建议<=10)
                 iPersonNum    - 输入，库中总人数(ps:模板库中一个人可能有多张模板图片，所以总人数小于或等于图片总数)
                 iMatchThreshold  - 输入，比对分通过阈值（建议76）
                 iMatchThreshold  - 输入，图片质量评价阈值（建议50）
                 pImage		  - 输入，RGB图像数据
                 nImgWidth     - 输入，图像宽度
                 nImgHeight    - 输入，图像高度
                 iFaceNum      - 输入，人脸数，须申请为全局变量
                 pFaceInfo     - 输入/输出，人脸检测结果，须申请为全局变量
                             通过MXFaceInfoEx结构体的reCog/reCogId/reCogScore属性获取
     返	回：	0-成功，其他失败
     备	注：   配合trackFace一起使用
     *******************************************************************************************/
    public native int searchFace(byte[] pFaceFeaList, int iPictureNum, int[] pFaceNumList, int iPersonNum,
                                  int iMatchThreshold, int iQualityThreshold,
                                 byte[] pImage, int nImgWidth, int nImgHeight, int iFaceNum,int[] pFaceInfo);


    /****************************************************************************
     功  能：	单可见光活体检测
     参  数：
                 pImage      - 输入，可见光图像数据
                 nImgWidth   - 输入，图像宽度
                 nImgHeight  - 输入，图像高度
                 pFaceNum    - 输入，人脸数
                 pFaceInfo   - 输入/输出，检测结果
     返	回：	0-成功，其他-失败
     *****************************************************************************/
    public native int visLivenessDetect(byte[] pImage, int nImgWidth, int nImgHeight, int iFaceNum,int[] pFaceInfo);

    /****************************************************************************
     功  能：	单近红外活体检测
     参  数：
                 pImage      - 输入，可见光图像数据
                 nImgWidth   - 输入，图像宽度
                 nImgHeight  - 输入，图像高度
                 pFaceNum    - 输入，人脸数
                 pFaceInfo   - 输入/输出，检测结果
     返	回：	0-成功，其他-失败
     *****************************************************************************/
    public native int nirLivenessDetect(byte[] pImage, int nImgWidth, int nImgHeight, int iFaceNum,int[] pFaceInfo);
}
