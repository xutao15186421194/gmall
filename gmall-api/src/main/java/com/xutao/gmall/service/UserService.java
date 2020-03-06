package com.xutao.gmall.service;

import com.xutao.gmall.bean.UmsMember;
import com.xutao.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {

    List<UmsMember> list();

    /**
     * 查询登录
     * @param umsMember
     * @return
     */
    UmsMember login(UmsMember umsMember);

    /**
     * 将token保存到redis中
     * @param token
     * @param memberId
     */
    void addUserToken(String token, String memberId);

    /**
     * 查询微博用户是否授权于本商城
     * @param umsCheck
     * @return
     */
    UmsMember checkOauthUser(UmsMember umsCheck);

    /**
     * 将授权用户保存数据库中
     * @param umsMember
     */
    void addOauthUser(UmsMember umsMember);

    /**
     * 根据用户id查询收获地址
     * @param memberId
     * @return
     */
    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    /**
     * 查询收货地址
     * @param receiveAddressId
     * @return
     */
    UmsMemberReceiveAddress getReceiveAddressById(String receiveAddressId);
}
