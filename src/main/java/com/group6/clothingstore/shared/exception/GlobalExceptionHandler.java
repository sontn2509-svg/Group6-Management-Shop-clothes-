package com.group6.clothingstore.shared.exception;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Bắt các exception chưa được xử lý trong Controller và trả về
 * trang lỗi Thymeleaf thay vì Whitelabel Error Page mặc định.
 * <p>
 * Các lỗi nghiệp vụ (IllegalArgumentException) vẫn được từng Controller
 * tự bắt và đẩy errorMessage vào view — handler này chỉ dự phòng cho lỗi hệ thống.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ResponseStatusException (404, 400...) do controller chủ động ném.
     * Spring đã resolve, nhưng nếu rơi vào đây thì vẫn trả trang lỗi đúng status.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, Model model,
                                       HttpServletResponse response) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        int code = status != null ? status.value() : 500;
        response.setStatus(code);
        model.addAttribute("status", code);
        model.addAttribute("error", status != null ? status.getReasonPhrase() : "Error");
        model.addAttribute("message", ex.getReason());
        log.warn("ResponseStatusException {}: {}", code, ex.getReason());
        return code == 404 ? "error/404" : "error";
    }

    /** 404 do không tìm thấy static resource / handler */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Not Found");
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    /** 405 — gọi sai method (vd gõ URL POST vào thanh địa chỉ → GET) */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, Model model) {
        model.addAttribute("status", 405);
        model.addAttribute("error", "Method Not Allowed");
        model.addAttribute("message",
                "Hành động này phải được thực hiện qua nút/form trên trang, không gõ trực tiếp vào thanh địa chỉ.");
        return "error";
    }

    /** 400 — sai kiểu tham số (vd /orders/abc thay vì /orders/1) hoặc thiếu param bắt buộc */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("error", "Bad Request");
        model.addAttribute("message", "Yêu cầu không hợp lệ hoặc thiếu tham số: " + ex.getMessage());
        return "error";
    }

    /** Fallback cho mọi lỗi hệ thống còn lại → 500.
     *  Lưu ý: KHÔNG bắt AccessDeniedException / AuthenticationException —
     *  để Spring Security tự xử lý (redirect login / access denied handler).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleAll(Exception ex, Model model) throws Exception {
        // Re-throw các exception của Security để Security chain xử lý đúng
        if (ex instanceof org.springframework.security.access.AccessDeniedException
                || ex instanceof org.springframework.security.core.AuthenticationException) {
            throw ex;
        }
        log.error("Unhandled server error", ex);
        model.addAttribute("status", 500);
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", "Đã có lỗi xảy ra trên hệ thống. Vui lòng thử lại sau.");
        return "error";
    }
}
