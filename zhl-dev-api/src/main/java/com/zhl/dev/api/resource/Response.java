package com.zhl.dev.api.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 *
 * @author zhl
 * @since 2026-02-04 10:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Response<T> implements Serializable {
    private String code;
    private String info;
    private T data;
}
