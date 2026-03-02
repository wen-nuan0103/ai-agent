package com.xuenai.aiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ReAct (Reasoning and Acting) 代理
 * 实现了思考 - 行动的循环模式
 */

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /**
     * 除了当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true 表示需要执行，false 表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行行动
     *
     * @return 行动结果
     */
    public abstract String act();

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
               return "No action needed"; 
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
