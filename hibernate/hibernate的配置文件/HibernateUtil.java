package com.mipo.util;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil{
	private static SessionFactory sessionFactory;
	static{   //只会在这个类在加载的时候，当虚拟机在加载这个类的时候会执行这段代码，之后这段代码就不会再执行了
		Configuration cfg=new Configuration();
		cfg.configure();   //配置对象会读取hibernate.cfg.xml配置文件
		sessionFactory=cfg.buildSessionFactory();   //构造了一个SessionFactory对象
	}

	private HibernateUtil(){
		
	}
	public static SessionFactory getSessionFactory(){
		return sessionFactory;
	}

	public static Session getSession(){
		return sessionFactory.openSession();
	}

	public static void add(Object obj){
		Session session=null;
		Transaction tran=null;
		try{
			session=HibernateUtil.getSession();
			tran=session.beginTransaction();
			session.save(obj);
			tran.commit();
		}finally{
			if(session!=null)
				session.close();
		}
	}

	public static void add1(Object obj){
		Session session=null;
		Transaction tran=null;
		try{
			session=HibernateUtil.getSession();
			tran=session.beginTransaction();
			session.save(obj);
			tran.commit();
		}catch(HibernateException e){  //所有调用hebernate方法的时候都可能会报这个异常
			if(tran!=null){
				tran.rollback();   //事务回滚，一般数据库无法收到你的提交请求的话，默认的会回滚，所以可以不用管这个异常，catch块可以省掉
			}
			throw e;
		}
		finally{
			if(session!=null){
				session.close();
			}	
		}
	}

	public static void delete(Object obj){
		Session session=null;
		Transaction tran=null;
		try{
			session=HibernateUtil.getSession();
			tran=session.beginTransaction();
			session.delete(obj);
			tran.commit();
		}finally{
			if(session!=null){
				session.close();
			}
		}
	}

	public static void update(Object obj){
		Session session=null;
		Transaction tran=null;
		try{
			session=HibernateUtil.getSession();
			tran=session.beginTransaction();
			session.update(obj);
			tran.commit();
		}finally{
			if(session!=null){
				session.close();
			}
				
		}
	}

	public static Object get(Class clazz,Serializable id){//class对象封装了这个类的所有信息，包括属性和方法等。比如Class userClass=User.class这个实例不是我们构造出来的，是虚拟机构把这个类加载到内存的时候构造这个事例出来。class相当于User类的静态成员，是Object的一个静态成员。
		Session session=null;
		try{
			session=HibernateUtil.getSession();
			Object obj=session.get(clazz,id);//查询不需要打开事务，因为它不涉及修改数据库
			return obj;
		}finally{
			if(session!=null){
				session.close();
			}	
		}
	}

}