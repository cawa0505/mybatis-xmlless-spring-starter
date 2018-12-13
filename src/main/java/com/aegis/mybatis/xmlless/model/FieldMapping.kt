package com.aegis.mybatis.xmlless.model

import com.aegis.mybatis.xmlless.methods.BaseMethod.Companion.HANDLER_PREFIX
import com.aegis.mybatis.xmlless.methods.BaseMethod.Companion.PROPERTY_PREFIX
import com.aegis.mybatis.xmlless.methods.BaseMethod.Companion.PROPERTY_SUFFIX
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo
import org.apache.ibatis.type.TypeHandler

/**
 *
 * @author 吴昊
 * @since 0.0.1
 */
data class FieldMapping(
    /**  持久化类的属性名称 */
    val property: String,
    /**  对应数据库表的列名称 */
    val column: String,
    /**  mybatis的字段处理器 */
    val typeHandler: Class<out TypeHandler<*>>?,
    val tableFieldInfo: TableFieldInfo,
    val insertIgnore: Boolean,
    val updateIgnore: Boolean,
    val selectIgnore: Boolean,
    val joinInfo: JoinInfo?) {

  fun getPropertyExpression(prefix: String? = null): String {
    return PROPERTY_PREFIX + (prefix ?: "") + property + (if (typeHandler != null) {
      ", $HANDLER_PREFIX" + typeHandler.name
    } else {
      ""
    }) + PROPERTY_SUFFIX
  }

  fun getUpdateExpression(): String {
    if (tableFieldInfo.update.isNullOrBlank()) {
      return getPropertyExpression()
    }
    return tableFieldInfo.update
  }

}
