
package org.zz.api;

import android.content.Context;
import android.util.Log;

import org.zz.jni.JustouchFaceApi;

import java.util.Calendar;

public class MXFaceAPI {

	private boolean      m_bInit = false;
	private JustouchFaceApi m_dllFaceApi = new JustouchFaceApi();

	/**
	 * @author   chen.gs
	 * @category 获取算法版本
	 * @param
	 * @return   算法版本
	 * */
	public String mxAlgVersion() {
		return m_dllFaceApi.getAlgVersion();
	}

	/**
	 * @author   chen.gs
	 * @category 初始化算法
	 * @param    context       - 输入，上下文句柄
	 * 			 szModelPath   - 输入，模型路径
	 * 			 szLicense     - 输入，授权码
	 * @return   0-成功，其他-失败
	 * */
	public int mxInitAlg(Context context,String szModelPath,String szLicense)
	{
		int nRet = 0;
		nRet = m_dllFaceApi.initAlg(context,szModelPath,szLicense);
		if(nRet!=0){
			Log.e("MIAXIS","initAlg nRet="+nRet);
			return nRet;
		}
		m_bInit = true;
		return 0;
	}


	/**
	 * @author   chen.gs
	 * @category 释放算法
	 * @param
	 * @return   0-成功，其他-失败
	 * */
	public int mxFreeAlg() {
		if (m_bInit==true) {
			m_dllFaceApi.freeAlg();
			m_bInit = false;
		}
		return 0;
	}

	/**
	 * @author   chen.gs
	 * @category 获取人脸特征长度
	 * @param
	 * @return   人脸特征长度
	 * */
	public int mxGetFeatureSize()
	{
		int iFeaLen = 0;
		if (m_bInit==true){
			iFeaLen = m_dllFaceApi.getFeatureSize();
		}
		return iFeaLen;
	}

	/**
	 * @author   chen.gs
	 * @category 人脸检测,用于静态图像检测
	 * @param    pImage    - 输入，RGB图像数据
	 * 			 nWidth    - 输入，图像宽度
	 * 			 nHeight   - 输入，图像高度
	 * 			 pFaceNum  - 输入/输出，人脸数
	 * 			 pFaceInfo - 输出，人脸信息
	 * @return   0-成功，其他-失败
	 * */
	public int mxDetectFace(byte[] pImage, int nWidth, int nHeight,
							int[] pFaceNum, MXFaceInfoEx[] pFaceInfo) {
		if (m_bInit!=true){
			return -10;
		}
		Calendar time1, time2;
		long bt_time;
		int nRet = -1;
		int[] bInfo = new int[MXFaceInfoEx.SIZE * MXFaceInfoEx.iMaxFaceNum];
		time1 = Calendar.getInstance();
		nRet = m_dllFaceApi.detectFace(pImage, nWidth, nHeight,pFaceNum,bInfo);
		time2 = Calendar.getInstance();
		bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
		//Log.e("MIAXIS", "dllDetectFace time="+bt_time);
		if(nRet!=0){
			//Log.e("MIAXIS", "No face Detected!");
			pFaceNum[0] = 0;
			return nRet;
		}
		//Log.e("MIAXIS", "FaceNum="+pFaceNum[0]);

		MXFaceInfoEx.Int2MXFaceInfoEx(pFaceNum[0],bInfo,pFaceInfo);

		return 0;
	}

	/**
	 * @author   chen.gs
	 * @category 检测人脸(跟踪人脸)，用于动态视频检测
	 * @param    pImage    - 输入，RGB图像数据
	 * 			 nWidth    - 输入，图像宽度
	 * 			 nHeight   - 输入，图像高度
	 * 			 pFaceNum  - 输入/输出，人脸数
	 * 			 pFaceInfo - 输出，人脸信息
	 * @return   0-成功，其他-失败
	 * */
	public int mxTrackFace(byte[] pImage, int nWidth, int nHeight,
						   int[] pFaceNum, MXFaceInfoEx[] pFaceInfo) {
		if (m_bInit!=true){
			return -10;
		}
		Calendar time1, time2;
		long bt_time;
		int nRet = -1;
		int[] bInfo = new int[MXFaceInfoEx.SIZE * MXFaceInfoEx.iMaxFaceNum];

		MXFaceInfoEx.MXFaceInfoEx2Int(pFaceNum[0],bInfo,pFaceInfo);

		time1 = Calendar.getInstance();
		nRet = m_dllFaceApi.trackFace(pImage, nWidth, nHeight,pFaceNum,bInfo);
		time2 = Calendar.getInstance();
		bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
		//Log.e("MIAXIS", "dllDetectFace time="+bt_time);
		if(nRet!=0){
			//Log.e("MIAXIS", "No face Detected!");
			pFaceNum[0] = 0;
			return nRet;
		}
		//Log.e("MIAXIS", "FaceNum="+pFaceNum[0]);

		MXFaceInfoEx.Int2MXFaceInfoEx(pFaceNum[0],bInfo,pFaceInfo);

		return 0;
	}

	/**
	 * @author   chen.gs
	 * @category 人脸特征提取
	 * @param    pImage       - 输入，RGB图像数据
	 * 			 nWidth       - 输入，图像宽度
	 * 			 nHeight      - 输入，图像高度
	 * 			 nFaceNum     - 输入，人脸个数
	 * 			 faceInfo     - 输入，人脸信息
	 * 			 pFeatureData - 输出，人脸特征，特征长度*人脸个数
	 * @return   0-成功，其他-失败
	 * */
	public int mxFeatureExtract(byte[] pImage, int nWidth, int nHeight,
                                int nFaceNum, MXFaceInfoEx[] pFaceInfo, byte[] pFeatureData)
	{
		if (m_bInit!=true){
			return -10;
		}
		//Log.e("MIAXIS", "mxFeatureExtract");
		int nRet = -1;
		int iFeatureSize = m_dllFaceApi.getFeatureSize();

		int[] bInfo = new int[MXFaceInfoEx.SIZE * MXFaceInfoEx.iMaxFaceNum];
		MXFaceInfoEx.MXFaceInfoEx2Int(nFaceNum,bInfo,pFaceInfo);

		nRet = m_dllFaceApi.featureExtract(pImage, nWidth, nHeight,nFaceNum, bInfo,pFeatureData);
		if(nRet!=0){
			return -11;
		}

		return nRet;
	}

	/**
	 * @author   chen.gs
	 * @category 人脸特征比对
	 * @param    pFaceFeaA - 输入，人脸特征A
	 * 			 pFaceFeaB - 输入，人脸特征B
	 * 			 fScore    - 输出，相似性度量值，0~1.0 ，越大越相似。
	 * @return   0-成功，其他-失败
	 * */
	public int mxFeatureMatch(byte[] pFaceFeaA,byte[] pFaceFeaB,float[] fScore)
	{
		if (m_bInit!=true){
			return -10;
		}
		return m_dllFaceApi.featureMatch(pFaceFeaA, pFaceFeaB,fScore);
	}

	/*******************************************************************************************
	 功	能：根据人脸检测结果，进行人脸图像质量评价
	 参	数：pImage     - 输入，RGB图像数据
	 nImgWidth     - 输入，图像宽度
	 nImgHeight    - 输入，图像高度
	 iFaceNum      - 输入，人脸个数
	 pMXFaceInfoEx - 输入，人脸检测结果
	 输出，根据瞳距进行从大到小排序
	 返	回：0-成功，-1 人脸过多(超过100个)，其他-失败
	 *******************************************************************************************/
	public int mxFaceQuality(byte[] pImage, int nWidth, int nHeight,
                             int nFaceNum, MXFaceInfoEx[] pFaceInfo){
		if (m_bInit!=true){
			return -10;
		}
		Calendar time1, time2;
		long bt_time;
		int nRet = -1;

		int[] bInfo = new int[MXFaceInfoEx.SIZE * MXFaceInfoEx.iMaxFaceNum];
		MXFaceInfoEx.MXFaceInfoEx2Int(nFaceNum,bInfo,pFaceInfo);

		time1 = Calendar.getInstance();
		nRet = m_dllFaceApi.faceQuality(pImage, nWidth, nHeight,nFaceNum,bInfo);
		time2 = Calendar.getInstance();
		bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
//		Log.e("MIAXIS", "zzFaceQualityRGB time="+bt_time);
		if(nRet!=0){
			return nRet;
		}

		MXFaceInfoEx.Int2MXFaceInfoEx(nFaceNum,bInfo,pFaceInfo);

		return nRet;
	}

	/*******************************************************************************************
	 功	能：根据人脸检测结果，计算人脸图像质量,用于人脸图像注册。
	 参	数：	pImage   - 输入，RGB图像数据
	 nWidth   - 输入，图像宽度
	 nHeight  - 输入，图像高度
	 iFaceNum      - 输入，人脸个数
	 pMXFaceInfoEx - 输入，人脸检测结果
	 返	回：质量分（建议采用高清USB摄像头采集，质量分大于95分，用于注册）
	 *******************************************************************************************/
	public int mxFaceQuality4Reg(byte[] pImage, int nWidth, int nHeight,
                                 int nFaceNum, MXFaceInfoEx[] pFaceInfo){
		if (m_bInit!=true){
			return -10;
		}
		Calendar time1, time2;
		long bt_time;
		time1 = Calendar.getInstance();
		int[] bInfo = new int[MXFaceInfoEx.SIZE * MXFaceInfoEx.iMaxFaceNum];
		MXFaceInfoEx.MXFaceInfoEx2Int(nFaceNum,bInfo,pFaceInfo);
		for(int i=0;i<nFaceNum;i++){
			int[] bInfoTmp = new int[MXFaceInfoEx.SIZE];
			System.arraycopy(bInfo, i* MXFaceInfoEx.SIZE, bInfoTmp, 0, MXFaceInfoEx.SIZE);
			m_dllFaceApi.faceQuality4Reg(pImage,nWidth,nHeight,bInfoTmp);
			pFaceInfo[i].quality = bInfoTmp[8 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
		}
		time2 = Calendar.getInstance();
		bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
		Log.e("MIAXIS", "dllFaceRegisterRGB time="+bt_time);
		return 0;
	}

	/****************************************************************************
	 功  能：	单可见光活体检测
	 参  数：
	 pImage      - 输入，可见光图像数据
	 nImgWidth   - 输入，图像宽度
	 nImgHeight  - 输入，图像高度
	 pFaceNum    - 输入，人脸数
	 pFaceInfo   - 输入，人脸检测结果
	 score       - 输入，活体比对分阈值
	 返	回：	0-非活体，1-活体，其他-失败
	 *****************************************************************************/
	public int mxVISLivenessDetect(byte[] pImage, int nImgWidth, int nImgHeight, int iFaceNum, MXFaceInfoEx[] pFaceInfo)
	{
		if (m_bInit!=true){
			return -10;
		}
		Calendar time1, time2;
		long bt_time;
		int nRet = -1;
		int[] bInfo = new int[MXFaceInfoEx.SIZE * iFaceNum];
		MXFaceInfoEx.MXFaceInfoEx2Int(iFaceNum,bInfo,pFaceInfo);
		time1 = Calendar.getInstance();
		nRet = m_dllFaceApi.visLivenessDetect(pImage, nImgWidth, nImgHeight,iFaceNum,bInfo);
		time2 = Calendar.getInstance();
		bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
		MXFaceInfoEx.Int2MXFaceInfoEx(iFaceNum,bInfo,pFaceInfo);

		return 0;
	}

	/****************************************************************************
	 功  能：	单近红外活体检测
	 参  数：
	 pImage      - 输入，可见光图像数据
	 nImgWidth   - 输入，图像宽度
	 nImgHeight  - 输入，图像高度
	 pFaceNum    - 输入，人脸数
	 pFaceInfo   - 输入，人脸检测结果
	 score       - 输入，活体比对分阈值
	 返	回：	0-非活体，1-活体，其他-失败
	 *****************************************************************************/
	public int mxNIRLivenessDetect(byte[] pImage, int nImgWidth, int nImgHeight, int iFaceNum, MXFaceInfoEx[] pFaceInfo)
	{
		if (m_bInit!=true){
			return -10;
		}
		Calendar time1, time2;
		long bt_time;
		int nRet = -1;
		int[] bInfo = new int[MXFaceInfoEx.SIZE * iFaceNum];
		MXFaceInfoEx.MXFaceInfoEx2Int(iFaceNum,bInfo,pFaceInfo);
		time1 = Calendar.getInstance();
		nRet = m_dllFaceApi.nirLivenessDetect(pImage, nImgWidth, nImgHeight,iFaceNum,bInfo);
		time2 = Calendar.getInstance();
		bt_time = time2.getTimeInMillis() - time1.getTimeInMillis();
		MXFaceInfoEx.Int2MXFaceInfoEx(iFaceNum,bInfo,pFaceInfo);
		return 0;
	}

}
