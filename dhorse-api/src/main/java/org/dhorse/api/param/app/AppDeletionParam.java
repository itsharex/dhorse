package org.dhorse.api.param.app;

import java.io.Serializable;

/**
 * 删除应用参数模型。
 * 
 * @author Dahai 2021-09-08
 */
public class AppDeletionParam implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 应用编号
	 */
	private String appId;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

}