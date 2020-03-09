package com.miaxis.thermal.data.dto;

import com.miaxis.thermal.data.exception.MyException;

public interface Mapper<T>{
   T transform() throws MyException;
}