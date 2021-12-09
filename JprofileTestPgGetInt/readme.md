# 使用Jprofiler对比getIntSimple和org.postgresql.jdbc.PgResultSet.getInt的性能对比


postgres中对结果集字段转化为int类型的处理方式，具体可以查看 org.postgresql.jdbc.PgResultSet
* 这里如果是小数，那么处理会有一定的消耗