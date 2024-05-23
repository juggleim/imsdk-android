package com.jet.im.log.action;

/**
 * @author Ye_Guli
 * @create 2024-05-23 9:06
 */
interface IAction {
   boolean isValid();

   @ActionTypeEnum.ActionType
   int getType();
}