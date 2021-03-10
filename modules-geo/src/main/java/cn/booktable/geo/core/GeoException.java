package cn.booktable.geo.core;

/**
 * @author ljc
 */
public class GeoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code=null;

    public GeoException(){
        super();
    }

    /**
     * 异常信息
     * @param msg
     */
    public GeoException(String msg)
    {
        super(msg);
    }

    public GeoException(Integer code ,String msg)
    {
        super(msg);
        this.setCode(code);
    }

    public GeoException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GeoException(Throwable cause) {
        super(cause);
    }

    /**
     * 错误编号
     * @param code
     */
    public void setCode(Integer code)
    {
        this.code=code;
    }

    /**
     * 错误编号
     * @return
     */
    public Integer getCode()
    {
        return this.code;
    }
}