package org.zz.api;

public class MXFaceInfoEx {
	public static final int iMaxFaceNum        = 100;
	public static final int MAX_KEY_POINT_NUM  = 120;
	public static final int SIZE = (22+2*MAX_KEY_POINT_NUM);
	//face rect人脸框
	public int x;        // 左上角x坐标
	public int y;        // 坐上角y坐标
	public int width;    // 人脸宽
	public int height;   // 人脸高

	//face_point关键点
	public int keypt_num;						            // 关键点个数                               // 关键点得分
	public int[] keypt_x = new int[MAX_KEY_POINT_NUM];      // 关键点x坐标
	public int[] keypt_y = new int[MAX_KEY_POINT_NUM];      // 关键点y坐标

	// 人脸属性
	public int age;       	//年龄
	public int gender;    	//性别
	public int expression; //表情

	//人脸质量分
	public int quality;     // 总分，0~100之间，越大则人脸质量越好.

	//20180925 add by chengs
	public int eyeDistance; 	// 瞳距
	public int liveness; 		// 活体
	public int detected;		// 1: 检测到的人脸,.0：跟踪到的人脸
	// 注： 跟踪到的仅ID和人脸框数据有效
	public int trackId;			// 人脸ID（ID<0表示没有进入跟踪）

	//20181120 add by chengs
	public int idmax;              //获取交并比最大的人脸下标
	public int reCog;              //判断该人脸是否被识别-识别标识
	public int reCogId;
	public int reCogScore;

	//保留位
	public int reserved1;
	public int reserved2;


	// head_pose头部姿态
	public int pitch;         // 抬头、低头,范围-90到90，越大表示越抬头
	public int yaw;           // 左右转头
	public int roll;          // 平面内偏头

	public MXFaceInfoEx() {
	}

	public MXFaceInfoEx(MXFaceInfoEx mxFaceInfoEx) {
		this.x = mxFaceInfoEx.x;
		this.y = mxFaceInfoEx.y;
		this.width = mxFaceInfoEx.width;
		this.height = mxFaceInfoEx.height;
		this.keypt_num = mxFaceInfoEx.keypt_num;
		this.age = mxFaceInfoEx.age;
		this.gender = mxFaceInfoEx.gender;
		this.expression = mxFaceInfoEx.expression;
		this.quality = mxFaceInfoEx.quality;
		this.eyeDistance = mxFaceInfoEx.eyeDistance;
		this.liveness = mxFaceInfoEx.liveness;
		this.detected = mxFaceInfoEx.detected;
		this.trackId = mxFaceInfoEx.trackId;
		this.idmax = mxFaceInfoEx.idmax;
		this.reCog = mxFaceInfoEx.reCog;
		this.reCogId = mxFaceInfoEx.reCogId;
		this.reCogScore = mxFaceInfoEx.reCogScore;
		this.reserved1 = mxFaceInfoEx.reserved1;
		this.reserved2 = mxFaceInfoEx.reserved2;
		this.pitch = mxFaceInfoEx.pitch;
		this.yaw = mxFaceInfoEx.yaw;
		this.roll = mxFaceInfoEx.roll;
		this.keypt_x = new int[MAX_KEY_POINT_NUM];
		System.arraycopy(mxFaceInfoEx.keypt_x, 0, this.keypt_x, 0, this.keypt_x.length);
		this.keypt_y = new int[MAX_KEY_POINT_NUM];
		System.arraycopy(mxFaceInfoEx.keypt_y, 0, this.keypt_y, 0, this.keypt_y.length);
	}

	public static int Int2MXFaceInfoEx(int iFaceNum, int[] iFaceInfo, MXFaceInfoEx[] pMXFaceInfoEx) {
		for (int i = 0; i < iFaceNum; i++) {
			pMXFaceInfoEx[i].x = iFaceInfo[i * MXFaceInfoEx.SIZE];
			pMXFaceInfoEx[i].y = iFaceInfo[i * MXFaceInfoEx.SIZE + 1];
			pMXFaceInfoEx[i].width = iFaceInfo[i * MXFaceInfoEx.SIZE + 2];
			pMXFaceInfoEx[i].height = iFaceInfo[i * MXFaceInfoEx.SIZE + 3];
			pMXFaceInfoEx[i].keypt_num = iFaceInfo[i * MXFaceInfoEx.SIZE + 4];
			for (int j = 0; j < pMXFaceInfoEx[i].keypt_num; j++) {
				pMXFaceInfoEx[i].keypt_x[j] = iFaceInfo[i * MXFaceInfoEx.SIZE + 5 + j];
				pMXFaceInfoEx[i].keypt_y[j] = iFaceInfo[i * MXFaceInfoEx.SIZE + 5 + j + MXFaceInfoEx.MAX_KEY_POINT_NUM];
			}
			pMXFaceInfoEx[i].age = iFaceInfo[i* MXFaceInfoEx.SIZE + 5 + 2* MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].gender = iFaceInfo[i* MXFaceInfoEx.SIZE + 6 + 2* MXFaceInfoEx.MAX_KEY_POINT_NUM] ;
			pMXFaceInfoEx[i].expression = iFaceInfo[i* MXFaceInfoEx.SIZE + 7 + 2* MXFaceInfoEx.MAX_KEY_POINT_NUM] ;
			pMXFaceInfoEx[i].quality = iFaceInfo[i * MXFaceInfoEx.SIZE + 8 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].eyeDistance = iFaceInfo[i * MXFaceInfoEx.SIZE + 9 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].liveness = iFaceInfo[i * MXFaceInfoEx.SIZE + 10 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].detected = iFaceInfo[i * MXFaceInfoEx.SIZE + 11 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].trackId = iFaceInfo[i * MXFaceInfoEx.SIZE + 12 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].idmax = iFaceInfo[i * MXFaceInfoEx.SIZE + 13 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].reCog = iFaceInfo[i * MXFaceInfoEx.SIZE + 14 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].reCogId = iFaceInfo[i * MXFaceInfoEx.SIZE + 15 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].reCogScore = iFaceInfo[i * MXFaceInfoEx.SIZE + 16 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].reserved1 = iFaceInfo[i * MXFaceInfoEx.SIZE + 17 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].reserved2 = iFaceInfo[i * MXFaceInfoEx.SIZE + 18 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].pitch = iFaceInfo[i * MXFaceInfoEx.SIZE + 19 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].yaw = iFaceInfo[i * MXFaceInfoEx.SIZE + 20 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
			pMXFaceInfoEx[i].roll = iFaceInfo[i * MXFaceInfoEx.SIZE + 21 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM];
		}
		return 0;
	}

	public static int MXFaceInfoEx2Int(int iFaceNum, int[] iFaceInfo, MXFaceInfoEx[] pMXFaceInfoEx) {
		for (int i = 0; i < iFaceNum; i++) {
			iFaceInfo[i * MXFaceInfoEx.SIZE]     = pMXFaceInfoEx[i].x;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 1] = pMXFaceInfoEx[i].y;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 2] = pMXFaceInfoEx[i].width;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 3] = pMXFaceInfoEx[i].height;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 4] = pMXFaceInfoEx[i].keypt_num;
			for (int j = 0; j < pMXFaceInfoEx[i].keypt_num; j++) {
				iFaceInfo[i * MXFaceInfoEx.SIZE + 5 + j] = pMXFaceInfoEx[i].keypt_x[j];
				iFaceInfo[i * MXFaceInfoEx.SIZE + 5 + j + MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].keypt_y[j];
			}
			iFaceInfo[i * MXFaceInfoEx.SIZE+ 5 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].age;
			iFaceInfo[i * MXFaceInfoEx.SIZE+ 6 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].gender;
			iFaceInfo[i * MXFaceInfoEx.SIZE+ 7 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].expression;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 8 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].quality;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 9 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].eyeDistance;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 10 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].liveness;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 11 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].detected;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 12 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].trackId;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 13 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].idmax;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 14 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].reCog;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 15 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].reCogId;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 16 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].reCogScore;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 17 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].reserved1;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 18 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].reserved2;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 19 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].pitch;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 20 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].yaw;
			iFaceInfo[i * MXFaceInfoEx.SIZE + 21 + 2 * MXFaceInfoEx.MAX_KEY_POINT_NUM] = pMXFaceInfoEx[i].roll;
		}
		return 0;
	}
}
