package com.mipo.util;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil{
	private static SessionFactory sessionFactory;
	static{   //ֻ����������ڼ��ص�ʱ�򣬵�������ڼ���������ʱ���ִ����δ��룬֮����δ���Ͳ�����ִ����
		Configuration cfg=new Configuration();
		cfg.configure();   //���ö�����ȡhibernate.cfg.xml�����ļ�
		sessionFactory=cfg.buildSessionFactory();   //������һ��SessionFactory����
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
		}catch(HibernateException e){  //���е���hebernate������ʱ�򶼿��ܻᱨ����쳣
			if(tran!=null){
				tran.rollback();   //����ع���һ�����ݿ��޷��յ�����ύ����Ļ���Ĭ�ϵĻ�ع������Կ��Բ��ù�����쳣��catch�����ʡ��
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

	public static Object get(Class clazz,Serializable id){//class�����װ��������������Ϣ���������Ժͷ����ȡ�����Class userClass=User.class���ʵ���������ǹ�������ģ�������������������ص��ڴ��ʱ�����������������class�൱��User��ľ�̬��Ա����Object��һ����̬��Ա��
		Session session=null;
		try{
			session=HibernateUtil.getSession();
			Object obj=session.get(clazz,id);//��ѯ����Ҫ��������Ϊ�����漰�޸����ݿ�
			return obj;
		}finally{
			if(session!=null){
				session.close();
			}	
		}
	}

}