package org.dhorse.infrastructure.param;

/**
 * 副本指标
 * 
 * @author Dahai 2023-02-28
 */
public class ReplicaMetricsParam extends PageParam {

	private static final long serialVersionUID = 1L;

	/**
	 * 副本名称
	 */
	private String replicaName;

	/**
	 * 指标类型，1：CPU（m），2：内存（MB）
	 */
	private Integer metricsType;

	/**
	 * 最小值
	 */
	private Long minValue;

	/**
	 * 最大值
	 */
	private Long maxValue;

	/**
	 * 使用值
	 */
	private Long usedValue;
	
	/**
	 * 开始时间
	 */
	private String startTime;
	
	/**
	 * 结束时间
	 */
	private String endTime;

	public String getReplicaName() {
		return replicaName;
	}

	public void setReplicaName(String replicaName) {
		this.replicaName = replicaName;
	}

	public Integer getMetricsType() {
		return metricsType;
	}

	public void setMetricsType(Integer metricsType) {
		this.metricsType = metricsType;
	}

	public Long getMinValue() {
		return minValue;
	}

	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}

	public Long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}

	public Long getUsedValue() {
		return usedValue;
	}

	public void setUsedValue(Long usedValue) {
		this.usedValue = usedValue;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	
}
