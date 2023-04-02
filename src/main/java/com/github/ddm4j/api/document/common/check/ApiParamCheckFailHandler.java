package com.github.ddm4j.api.document.common.check;

import com.github.ddm4j.api.document.common.exception.bean.ApiCheckInfo;

import java.util.List;

/**
 * 校验失败处理类
 */
public interface ApiParamCheckFailHandler {
    /**
     * 校验异常处理方法
     *
     * @param infoList 异常校验失败集合
     * @return null 表示忽略，继续向下执行，反之为拦截，不执行
     */
    Object checkApiParamFail(List<ApiCheckInfo> infoList);
}
