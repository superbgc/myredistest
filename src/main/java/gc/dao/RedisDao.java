package gc.dao;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import gc.entity.Student;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDao {
	private final JedisPool jedisPool;
	private final JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
	private RuntimeSchema<Student> schema=RuntimeSchema.createFrom(Student.class);
	public RedisDao(String ip,int port) {
		// TODO Auto-generated constructor stub
		jedisPool=new JedisPool(jedisPoolConfig,ip, port,60,"rrg88888");
		
	}
	public String getString(String key){
		Jedis jedis=jedisPool.getResource();
		byte[] bytes=jedis.get(key.getBytes());
		String string=new String(bytes);
		return string;
		
	}
	public String setString(String key,String value){
		Jedis jedis=jedisPool.getResource();
		byte[] values=value.getBytes();
		
		return jedis.set(key.getBytes(), values);
		
	}
	public Student getStudent(int id){
		Jedis jedis=null;
		try {
			
			 jedis=jedisPool.getResource();
			String key="student_"+id;
			//通过key从redis中拿到字节码文件
			byte[] bytes=jedis.get(key.getBytes());
			//通过反序列化将字节码文件转换为对象
			if(bytes!=null){
				//获取到缓存
				//获取一个空对象
				Student student=schema.newMessage();
				//Student对象被反序列化
				ProtostuffIOUtil.mergeFrom(bytes, student, schema);
				return student;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			jedis.close();
		}
		//采用自定义序列化  protosuff框架：序列化对象必须是一个javabean
		
		return null;
	}
	public String setStudent(Student student){
		Jedis jedis=null;
		try {
			//拿到对象 转换为byte[]   序列化byte[]
			jedis=jedisPool.getResource();
			String key="student_"+student.getId();
			byte[] bytes=ProtostuffIOUtil.toByteArray(student, schema,
					//缓存器
					LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
			//成功返回ok  错误会返回错误信息
			String result=jedis.setex(key.getBytes(),3600, bytes);
			return result;
		} finally {
			jedis.close();
			// TODO: handle finally clause
		}
		
	}
	public static void main(String[] args) {
		RedisDao redisDao=new RedisDao("127.0.0.1", 6379);
		/*Student student=new Student();
		student.setId(123);
		student.setName("Superb");
		System.out.println(redisDao.setStudent(student));*/
		
		//System.out.println(redisDao.setString("token", "nkjnlnkmmkmlkmln"));
		long a=System.currentTimeMillis();
		System.out.println(redisDao.getString("token"));
		System.out.println(System.currentTimeMillis()-a);
		
		/*long a=System.currentTimeMillis();
		System.out.println(redisDao.getStudent(123));
		System.out.println(System.currentTimeMillis()-a);*/
	}
	
}
