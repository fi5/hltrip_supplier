package com.huoli.trip.supplier.self.yaochufa.constant;

public enum YcfOrderEnum {

    /**
     * 0:待支付：创建订单成功，合作方尚未付款。
     */
    UNPAID{
        public int code() {
            return 0;
        }

        public String describe() {
            return "待支付";
        }
    },
    /**
     *10: 待确认：支付订单成功，要出发确认流程中
     */
    PAY_CONFIRMED{
        public int code() {
            return 10;
        }

        public String describe() {
            return "待确认";
        }
    },

    /**
     * 11:待确认（申请取消）：合作方申请取消，要出发在审核状态
     */
    CANCEL_CONFIRMED{
        public int code() {
            return 11;
        }

        public String describe() {
            return "待确认";
        }
    },
    /**
     * 12:[全网预售特有]预约出行：待二次预约
     */
    SECOND_APPOINTMENT{
        public int code() {
            return 12;
        }

        public String describe() {
            return "待二次预约";
        }
    },
    /**
     * 13:[全网预售特有]立即补款：待二次预约补款
     */
    SECOND_APPOINTMENT_PAY{
        public int code() {
            return 13;
        }

        public String describe() {
            return "待二次预约补款";
        }
    },
    /**
     * 20:待出行：已确认订单，客人可出行消费；全网预售：预约成功，可出行
     */
    TO_TRAVEL{
        public int code() {
            return 20;
        }

        public String describe() {
            return "待出行";
        }
    },
    /**
     * 30:已消费：客人已消费订单
     */
    SPENDING{
        public int code() {
            return 30;
        }

        public String describe() {
            return "已消费";
        }
    },
    /**
     * 	已取消：订单已取消
     */
    CANCLE{
        public int code() {
            return 40;
        }

        public String describe() {
            return "已取消";
        }
    }



}
