package com.cn.com.cqucc.forum.util;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤工具类
 */

@Component
public class SensitiveFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);
    //替换符
    private static final String REPLACEMENT = "***";

    private TrieNode rootNode = new TrieNode();

    //初始化敏感词文件
    @PostConstruct
    private void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWord;
            while ((keyWord = br.readLine()) != null) {
                // 将读取的敏感词存入到前缀树中
                this.addKeyWord(keyWord);
                //System.out.println(keyWord);
            }
        } catch (IOException e) {
            LOGGER.error("读取敏感词文件失败：" + e.getMessage());
        }
        //需要将字节流转换为字符流
    }

    private void addKeyWord(String keyWord) {
        TrieNode tempNode = rootNode; // 指向根节点

        // 遍历String
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TrieNode subNodes = tempNode.getSubNodes(c);
            // 判断给节点是否在前缀树中已经存在
            if (subNodes == null) {
                // 初始化子节点
                subNodes = new TrieNode();
                tempNode.addSubNode(c, subNodes);
            }
            /*System.out.println(c);*/
            // 指向子节点，进入下一轮循环
            tempNode = subNodes;
            // 设置结束标记
            if (i == keyWord.length() - 1) {
                tempNode.setKeyWordsEnd(true);
            }
        }
    }


    /**
     * @param text 带过滤的文本
     * @return 返回过滤完成之后的文本
     */
    public String filter(String text) {
        //指针1
        TrieNode tempNode = rootNode; // 指向根节点
        //指针2
        int begin = 0;
        //指针3
        int position = 0;

        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            if (!isSymbol(c)) {
                //如果指针1处于根节点，将此节点计入结果，让指针2下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或者是中间，指针3都向下走
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNodes(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeyWordsEnd()) {
                // 发现敏感词，将begin~position字符串替换掉
                sb.append(REPLACEMENT);
                //指针进入下一个位置
                begin = ++position;
                //再重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                position++;
            }
        }
        // 将最后一批 字符计入到结果 : 就是当我们的结果 指针3到达终点但是指针2未到达终点的时候
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80 ~ 0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c > 0x2E80 || c > 0x9FFF);
    }

    //前缀树 结构 : 所有算法都是基于该数据结构
    private class TrieNode {

        //关键词结束表示
        private boolean isKeyWordsEnd = false;

        // 子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordsEnd() {
            return isKeyWordsEnd;
        }

        public void setKeyWordsEnd(boolean keyWordsEnd) {
            isKeyWordsEnd = keyWordsEnd;
        }

        // 添加节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        public TrieNode getSubNodes(Character c) {
            return subNodes.get(c);
        }
    }
}
