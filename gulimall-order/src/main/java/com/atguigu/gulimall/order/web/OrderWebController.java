package com.atguigu.gulimall.order.web;

import com.atguigu.common.exception.NoStockException;
import com.atguigu.gulimall.order.constant.OrderSubmitStatus;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.vo.OrderSubmitVo;
import com.atguigu.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
@RequiredArgsConstructor
public class OrderWebController {

    private final OrderService orderService;

    /**
     * 订单确认页
     * 通过{@link OrderService#confirmOrder()}获取确认页信息
     *
     * @param model
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 提交订单
     *
     * @param submitVo
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = null;
        String msg = "下单失败：";

        responseVo = orderService.submitOrder(submitVo);

        if (responseVo.getStatus() == OrderSubmitStatus.OK) {
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            msg += responseVo.getStatus().getMsg();
        }

        redirectAttributes.addFlashAttribute("msg", msg);
        return "redirect:http://order.gulimall.com/toTrade";
    }
}
