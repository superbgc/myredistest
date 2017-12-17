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
			//ͨ��key��redis���õ��ֽ����ļ�
			byte[] bytes=jedis.get(key.getBytes());
			//ͨ�������л����ֽ����ļ�ת��Ϊ����
			if(bytes!=null){
				//��ȡ������
				//��ȡһ���ն���
				Student student=schema.newMessage();
				//Student���󱻷����л�
				ProtostuffIOUtil.mergeFrom(bytes, student, schema);
				return student;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			jedis.close();
		}
		//�����Զ������л�  protosuff��ܣ����л����������һ��javabean
		
		return null;
	}
	public String setStudent(Student student){
		Jedis jedis=null;
		try {
			//�õ����� ת��Ϊbyte[]   ���л�byte[]
			jedis=jedisPool.getResource();
			String key="student_"+student.getId();
			byte[] bytes=ProtostuffIOUtil.toByteArray(student, schema,
					//������
					LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
			//�ɹ�����ok  ����᷵�ش�����Ϣ
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
