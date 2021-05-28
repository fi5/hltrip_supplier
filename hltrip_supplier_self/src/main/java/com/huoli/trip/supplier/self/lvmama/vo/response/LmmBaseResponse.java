package com.huoli.trip.supplier.self.lvmama.vo.response;

import com.huoli.trip.supplier.self.lvmama.vo.State;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lunatic
 * @Title:
 * @Package
 * @Description:
 * @date 2021/3/1515:36
 */
@Data
public class LmmBaseResponse implements Serializable {
    private State state;

    public static LmmBaseResponse success(){
        LmmBaseResponse response = new LmmBaseResponse();
        State stateResponse = new State();
        stateResponse.setCode("1000");
        stateResponse.setMessage("接收成功");
        response.setState(stateResponse);
        return response;
    }

    public static LmmBaseResponse fail(){
        LmmBaseResponse response = new LmmBaseResponse();
        State stateResponse = new State();
        stateResponse.setCode("10099");
        stateResponse.setMessage("接收失败");
        response.setState(stateResponse);
        return response;
    }
}
