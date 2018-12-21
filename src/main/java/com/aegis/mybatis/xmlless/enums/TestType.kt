package com.aegis.mybatis.xmlless.enums


/**
 * Created by 吴昊 on 2018/12/19.
 */
enum class TestType(val expression: String) {

  ArrayNotEmpty(".length > 0"),
  CollectionNotEmpty(".size() > 0"),
  EqFalse("= FALSE"),
  EqTrue(" = TRUE"),
  GtZero(" > 0"),
  GteZero(" >= 0"),
  IsNull(" = null"),
  LtZero(" < 0"),
  LteZero(" <= 0"),
  NotNull(" != null"),
  StringNotEmpty(".length() > 0");

}
