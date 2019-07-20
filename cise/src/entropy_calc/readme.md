任务：计算两个图的熵值
文件：
    1. data: 包含两个图各自的entity, attribute, relation信息
    2. match: 包含三种不同的图融合规则
Data: 每条数据占一行，行内数据用"|"分隔
    entity格式: entityId|entityName, 两个元素。表示一个名称（有向）。其中entity_2存在部分Id无name用__null__进行了处理
    attr格式: entityId|AttrType|Value，三个元素。表示一个属性（有向）
    rel格式: entityId|RelType|entityId，三个元素。表示一个关系（有向）
match：
    entityId|matchId，matchId可以认为是群体知识图谱中的id，matchId一致的entity在群体知识图谱中是融合的。三个文件分别表示的是两个图谱三种不同的融合关系：
    1. entity完全不相容
    2. 同名entity相容
    3. entity完全相容
融合原则：
    entity融合，根据match文件，生成三组不同的融合。可以认为所有的entity都是一个类型
    attr融合，同样的AttrType可以相融
    rel融合，同样的RelType可以相融
熵值计算：
    这个我不太清楚工具里具体是怎么处理的。
    可能分为了三部分：1.节点的出入熵 2. 总边数 3. 一个修正量
    如果确实是计算了这三者的话，输出一下不同计算方式下的熵值
    a. 节点的出入熵
    b. 节点的出入熵 * 总边数
    c. 节点的出入熵 * 总边数 * 修正量
相似度计算：
    对于值节点采用值的编辑距离进行计算
    对于集合的情况还是延用集合的交集除以并集
