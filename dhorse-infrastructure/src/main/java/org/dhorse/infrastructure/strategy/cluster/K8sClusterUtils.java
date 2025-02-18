package org.dhorse.infrastructure.strategy.cluster;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.dhorse.api.enums.MessageCodeEnum;
import org.dhorse.infrastructure.utils.Constants;
import org.dhorse.infrastructure.utils.K8sUtils;
import org.dhorse.infrastructure.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1ClusterRole;
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding;
import io.kubernetes.client.openapi.models.V1ClusterRoleList;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1DaemonSet;
import io.kubernetes.client.openapi.models.V1DaemonSetList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Role;
import io.kubernetes.client.openapi.models.V1RoleBinding;
import io.kubernetes.client.openapi.models.V1ServiceAccount;
import io.kubernetes.client.openapi.models.V1ServiceAccountList;
import io.kubernetes.client.util.Yaml;

public class K8sClusterUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(K8sClusterUtils.class);
	
	private static final String FILEBEAT_NAME = "filebeat";
	
	private static final String FILEBEAT_KUBEADM_NAME = "filebeat-kubeadm-config";
	
	private static final String FILEBEAT_CONFIG = "filebeat-config";
	
	private static final String FILEBEAT_LABEL_SELECTOR = "k8s-app=filebeat";
	
	private static final String FILEBEAT_NAMESPACE = K8sUtils.KUBE_SYSTEM_NAMESPACE;
	
	public static void openLogCollector(ApiClient apiClient) {
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		AppsV1Api appsApi = new AppsV1Api(apiClient);
		RbacAuthorizationV1Api rbacApi = new RbacAuthorizationV1Api(apiClient);
		File fileBeatFile = new File(Constants.CONF_PATH + "filebeat-k8s.yml");
		if (!fileBeatFile.exists()) {
			LogUtils.throwException(logger, MessageCodeEnum.FILE_BEAT_K8S_FILE_INEXISTENCE);
		}
		String label = FILEBEAT_LABEL_SELECTOR;
		String ns = FILEBEAT_NAMESPACE;
		try {
			List<Object> yamlObjects = Yaml.loadAll(fileBeatFile);
			for (Object o : yamlObjects) {
				//ServiceAccount
				if (o instanceof V1ServiceAccount) {
					V1ServiceAccountList list = coreApi.listNamespacedServiceAccount(ns, null,
							null, null, null, label, 1, null, null, null, null);
					if (CollectionUtils.isEmpty(list.getItems())) {
						coreApi.createNamespacedServiceAccount(ns, (V1ServiceAccount)o, null, null, null, null);
					} else {
						coreApi.replaceNamespacedServiceAccount(FILEBEAT_NAME, ns, (V1ServiceAccount) o, null, null, null, null);
					}
					continue;
				}
				
				//ClusterRole
				if (o instanceof V1ClusterRole) {
					V1ClusterRoleList list = rbacApi.listClusterRole(null, null, null, null, label, 1, null, null, null, null);
					if (CollectionUtils.isEmpty(list.getItems())) {
						rbacApi.createClusterRole((V1ClusterRole)o, null, null, null, null);
					} else {
						rbacApi.replaceClusterRole(FILEBEAT_NAME, (V1ClusterRole)o, null, null, null, null);
					}
					continue;
				}
				
				//Role
				if (o instanceof V1Role && FILEBEAT_NAME.equals(((V1Role) o).getMetadata().getName())) {
					V1Role role = null;
					try {
						role = rbacApi.readNamespacedRole(FILEBEAT_NAME, ns, null);
					}catch(ApiException e) {
						if(e.getCode() == 404) {
							rbacApi.createNamespacedRole(ns, (V1Role)o, null, null, null, null);
						}else {
							throw e;
						}
					}
					if (role != null) {
						rbacApi.replaceNamespacedRole(FILEBEAT_NAME, ns, (V1Role)o, null, null, null, null);
					}
					continue;
				}
				
				//Role2
				if (o instanceof V1Role && FILEBEAT_KUBEADM_NAME.equals(((V1Role) o).getMetadata().getName())) {
					V1Role role = null;
					try {
						role = rbacApi.readNamespacedRole(FILEBEAT_KUBEADM_NAME, ns, null);
					}catch(ApiException e) {
						if(e.getCode() == 404) {
							rbacApi.createNamespacedRole(ns, (V1Role)o, null, null, null, null);
						}else {
							throw e;
						}
					}
					if (role != null) {
						rbacApi.replaceNamespacedRole(FILEBEAT_KUBEADM_NAME, ns, (V1Role)o, null, null, null, null);
					}
					continue;
				}
				
				//ClusterRoleBinding
				if (o instanceof V1ClusterRoleBinding) {
					V1ClusterRoleBinding binding = null;
					try {
						binding = rbacApi.readClusterRoleBinding(FILEBEAT_NAME, null);
					}catch(ApiException e) {
						if(e.getCode() == 404) {
							rbacApi.createClusterRoleBinding((V1ClusterRoleBinding)o, null, null, null, null);
						}else {
							throw e;
						}
					}
					if (binding != null) {
						rbacApi.replaceClusterRoleBinding(FILEBEAT_NAME, (V1ClusterRoleBinding)o, null, null, null, null);
					}
					continue;
				}
				
				//RoleBinding
				if (o instanceof V1RoleBinding && FILEBEAT_NAME.equals(((V1RoleBinding) o).getMetadata().getName())) {
					V1RoleBinding binding = null;
					try {
						binding = rbacApi.readNamespacedRoleBinding(FILEBEAT_NAME, ns, null);
					}catch(ApiException e) {
						if(e.getCode() == 404) {
							rbacApi.createNamespacedRoleBinding(ns, (V1RoleBinding)o, null, null, null, null);
						}else {
							throw e;
						}
					}
					if (binding != null) {
						rbacApi.replaceNamespacedRoleBinding(FILEBEAT_NAME, ns, (V1RoleBinding)o, null, null, null, null);
					}
					continue;
				}
				
				//RoleBinding2
				if (o instanceof V1RoleBinding && FILEBEAT_KUBEADM_NAME.equals(((V1RoleBinding) o).getMetadata().getName())) {
					V1RoleBinding binding = null;
					try {
						binding = rbacApi.readNamespacedRoleBinding(FILEBEAT_KUBEADM_NAME, ns, null);
					}catch(ApiException e) {
						if(e.getCode() == 404) {
							rbacApi.createNamespacedRoleBinding(ns, (V1RoleBinding)o, null, null, null, null);
						}else {
							throw e;
						}
					}
					if (binding != null) {
						rbacApi.replaceNamespacedRoleBinding(FILEBEAT_KUBEADM_NAME, ns, (V1RoleBinding)o, null, null, null, null);
					}
					continue;
				}
				
				//ConfigMap
				if (o instanceof V1ConfigMap) {
					V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(ns, null,
							null, null, null, label, 1, null, null, null, null);
					if (CollectionUtils.isEmpty(configMapList.getItems())) {
						coreApi.createNamespacedConfigMap(ns, (V1ConfigMap) o, null, null, null,
								null);
					} else {
						coreApi.replaceNamespacedConfigMap(FILEBEAT_CONFIG, ns,
								(V1ConfigMap) o, null, null, null, null);
					}
					continue;
				}
				
				//DaemonSet
				if (o instanceof V1DaemonSet) {
					V1DaemonSetList daemonSetList = appsApi.listNamespacedDaemonSet(ns, null,
							null, null, null, label, 1, null, null, null, null);
					if (CollectionUtils.isEmpty(daemonSetList.getItems())) {
						appsApi.createNamespacedDaemonSet(ns, (V1DaemonSet) o, null, null, null,
								null);
					} else {
						appsApi.replaceNamespacedDaemonSet(FILEBEAT_NAME, ns, (V1DaemonSet) o,
								null, null, null, null);
					}
				}
			}
		} catch (ApiException e) {
			LogUtils.throwException(logger, e.getResponseBody(), MessageCodeEnum.FAILURE);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.FAILURE);
		}
	}
	
	public static void closeLogCollector(ApiClient apiClient) {
		CoreV1Api coreApi = new CoreV1Api(apiClient);
		AppsV1Api appsApi = new AppsV1Api(apiClient);
		RbacAuthorizationV1Api rbacApi = new RbacAuthorizationV1Api(apiClient);
		String label = FILEBEAT_LABEL_SELECTOR;
		String ns = FILEBEAT_NAMESPACE;
		try {
			//ServiceAccount
			V1ServiceAccountList list = coreApi.listNamespacedServiceAccount(ns, null,
					null, null, null, label, 1, null, null, null, null);
			if (!CollectionUtils.isEmpty(list.getItems())) {
				coreApi.deleteNamespacedServiceAccount(FILEBEAT_NAME, ns, null, null, null, null, null, null);
			}
			
			//ClusterRole
			V1ClusterRoleList list2 = rbacApi.listClusterRole(null, null, null, null,
					label, 1, null, null, null, null);
			if (!CollectionUtils.isEmpty(list2.getItems())) {
				rbacApi.deleteClusterRole(FILEBEAT_NAME, null, null, null, null, null, null);
			}
			
			//Role
			V1Role role = null;
			try {
				role = rbacApi.readNamespacedRole(FILEBEAT_NAME, ns, null);
			}catch(ApiException e) {
				if(e.getCode() != 404) {
					throw e;
				}
			}
			if (role != null) {
				rbacApi.deleteNamespacedRole(FILEBEAT_NAME, ns, null, null, null, null, null, null);
			}
			
			//Role2
			V1Role role2 = null;
			try {
				role2 = rbacApi.readNamespacedRole(FILEBEAT_KUBEADM_NAME, ns, null);
			}catch(ApiException e) {
				if(e.getCode() != 404) {
					throw e;
				}
			}
			if (role2 != null) {
				rbacApi.deleteNamespacedRole(FILEBEAT_KUBEADM_NAME, ns, null, null, null, null, null, null);
			}
			
			//ClusterRoleBinding
			V1ClusterRoleBinding roleBinding = null;
			try {
				roleBinding = rbacApi.readClusterRoleBinding(FILEBEAT_NAME, null);
			}catch(ApiException e) {
				if(e.getCode() != 404) {
					throw e;
				}
			}
			if (roleBinding != null) {
				rbacApi.deleteClusterRoleBinding(FILEBEAT_NAME, null, null, null, null, null, null);
			} 
			
			//RoleBinding
			V1RoleBinding binding = null;
			try {
				binding = rbacApi.readNamespacedRoleBinding(FILEBEAT_NAME, ns, null);
			}catch(ApiException e) {
				if(e.getCode() != 404) {
					throw e;
				}
			}
			if (binding != null) {
				rbacApi.deleteNamespacedRoleBinding(FILEBEAT_NAME, ns, null, null, null, null, null, null);
			}
			
			//RoleBindin2
			V1RoleBinding binding2 = null;
			try {
				binding2 = rbacApi.readNamespacedRoleBinding(FILEBEAT_KUBEADM_NAME, ns, null);
			}catch(ApiException e) {
				if(e.getCode() != 404) {
					throw e;
				}
			}
			if (binding2 != null) {
				rbacApi.deleteNamespacedRoleBinding(FILEBEAT_KUBEADM_NAME, ns, null, null, null, null, null, null);
			}
			
			//ConfigMap
			V1ConfigMapList configMapList = coreApi.listNamespacedConfigMap(ns, null, null,
					null, null, label, 1, null, null, null, null);
			if (!CollectionUtils.isEmpty(configMapList.getItems())) {
				coreApi.deleteNamespacedConfigMap(FILEBEAT_CONFIG, ns, null, null, null,
						null, null, null);
			}
			
			//DaemonSet
			V1DaemonSetList daemonSetList = appsApi.listNamespacedDaemonSet(ns, null, null,
					null, null, label, 1, null, null, null, null);
			if (!CollectionUtils.isEmpty(daemonSetList.getItems())) {
				appsApi.deleteNamespacedDaemonSet(FILEBEAT_NAME, ns, null, null, null, null,
						null, null);
			}
		} catch (ApiException e) {
			clusterError(e);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
	}
	
	public static boolean logSwitchStatus(ApiClient apiClient) {
		AppsV1Api appsApi = new AppsV1Api(apiClient);
		try {
			V1DaemonSetList daemonSetList = appsApi.listNamespacedDaemonSet(FILEBEAT_NAMESPACE, null, null,
					null, null, FILEBEAT_LABEL_SELECTOR, 1, null, null, null, null);
			return !CollectionUtils.isEmpty(daemonSetList.getItems());
		} catch (ApiException e) {
			clusterError(e);
		} catch (Exception e) {
			LogUtils.throwException(logger, e, MessageCodeEnum.CLUSTER_FAILURE);
		}
		return false;
	}
	
	public static V1ConfigMap dhorseConfigMap() {
		V1ConfigMap configMap = new V1ConfigMap();
		configMap.setApiVersion("v1");
		configMap.setKind("ConfigMap");
		V1ObjectMeta meta = new V1ObjectMeta();
		meta.setName(K8sUtils.DHORSE_CONFIGMAP_NAME);
		meta.setLabels(Collections.singletonMap(K8sUtils.DHORSE_LABEL_KEY, K8sUtils.DHORSE_CONFIGMAP_NAME));
		configMap.setMetadata(meta);
		return configMap;
	}
	
	private static void clusterError(ApiException e) {
		String message = e.getResponseBody() == null ? e.getMessage() : e.getResponseBody();
		if(message.contains("SocketTimeoutException")) {
			LogUtils.throwException(logger, message, MessageCodeEnum.CONNECT_CLUSTER_FAILURE);
		}
		LogUtils.throwException(logger, message, MessageCodeEnum.CLUSTER_FAILURE);
	}
}
