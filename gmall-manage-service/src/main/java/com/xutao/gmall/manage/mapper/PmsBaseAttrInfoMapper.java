package com.xutao.gmall.manage.mapper;

import com.xutao.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {

    List<PmsBaseAttrInfo> selectgetAttrValueListByValueId(@Param("valueIdStr") String valueIdStr);
}
