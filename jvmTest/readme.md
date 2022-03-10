# mat 查看GCRoots的示例

mat 查看GCRoots的示例
1 示例代码 需要有运行时栈变量，因为它会放入GCroot区域
2 示例代码，需要两个交互输入 scan.input()来停顿，这样可以前后转储两个快照来比较，使用jvisualvm来转储
3 使用MAT打开比较两个的gcroots