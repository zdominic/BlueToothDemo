package com.edan.www.bluetoothdemo;

import java.util.ArrayList;

public class BufQueue<T>
{
    private ArrayList<T> m_pBuff;// 数据缓冲区
    //private T m_arrBuf[] = null;
    private int          m_iCapacity;     // 该队列的容量，长度最大值
    public  int          m_iHead;         // 头索引
    public  int          m_iTail;         // 尾索引

    /**
     * 构造函数
     *
     * @param iCap: 输入参数，队列大小
     *
     * @return 无
     *
     * @author 李凤敏 2014.05.26
     */
    public BufQueue(int iCap)
    {
        if (iCap < 0)
        {
            return;
        }
        m_pBuff = new ArrayList<T>(iCap);
        m_iCapacity = iCap;
        m_iHead = 0;
        m_iTail = 0;

    }


    /**
     * 构造函数
     *
     * @param
     *
     * @return 无
     *
     * @author 李凤敏 2014.05.26
     */
    public BufQueue()
    {
        m_pBuff = null;
        m_iCapacity = 0;
        m_iHead = 0;
        m_iTail = 0;
    }

    /**
     * 弹出队列的第一个元素，也就是最老的一个数据
     *
     * @param
     *
     * @return 队列第一个元素
     *
     * @author 李凤敏 2014.05.26
     */
    public T Pop()
    {
        T Element;

        if (null == m_pBuff)
        {
            //Log.e("FxMonitor", "m_pBuff is 0.");
            return null;
        }

        if (Empty())
        {
            //Log.w("FxMonitor", "BufQueue is empty.");
            return null;
        }

        Element = m_pBuff.get(m_iHead);
        m_iHead = (m_iHead + 1) % m_iCapacity;

        return Element;
    }

    /**
     * 取出队列的第一个元素，和pop相比，不改变队列
     *
     * @param
     *
     * @return 队列第一个元素
     *
     * @author 李凤敏 2014.05.26
     */
    public T Front()
    {
        T Element;

        if (null == m_pBuff)
        {
            //Log.e("FxMonitor", "m_pBuff is 0.");
            return null;
        }

        if (Empty())
        {
            //Log.w("FxMonitor", "BufQueue is empty.");
            return null;
        }

        Element = m_pBuff.get(m_iHead);
        return Element;
    }

    /**
     * 从队列尾部添加一个元素
     *
     * @param Element:待添加元素
     *
     * @return 添加元素是否成功 -1 : 添加失败   0 : 正常添加
     *
     * @author 李凤敏 2014.05.26
     */
    public int Push(T Element)
    {
        if (null == m_pBuff)
        {
            //Log.e("FxMonitor", "m_pBuff is 0, push failed.");
            return -1;
        }

        if (Full() == true)
        {
            //Log.w("FxMonitor", "BufQueue if full, push failed.");
            return -1;
        }

        if (m_pBuff.size() < m_iCapacity)
        {
            m_pBuff.add(Element);
        }
        else
        {
            m_pBuff.set(m_iTail, Element);
        }

        m_iTail = (m_iTail + 1) % m_iCapacity;

        return 0;
    }

    /**
     * 取得最新的长度为len的数据，存入buff中，并将len个数据从队列中弹出
     *
     * @param SubBuf : 取得数据后存于该buff中
     * @param iLen  : 希望取得数据的长度
     *
     * @return 读取队列是否成功/读取个数 -1:读取失败   >=0:实际读取个数
     *
     * @author 李凤敏 2014.05.26
     */
    public int Read(T SubBuf[], int iLen)
    {
        int iReadSize;
        iReadSize = ReadTry(SubBuf, iLen);

        if (iReadSize == -1)
        {
            return -1;
        }

        m_iHead = (m_iHead + iReadSize) % m_iCapacity;
        return iReadSize;
    }

    /**
     * 取得最新的长度为len的数据，存入buff中
     *
     * @param SubBuf : 取得数据后存于该buff中
     * @param iLen  : 希望取得数据的长度
     *
     * @return 读取队列是否成功/读取个数 -1:读取失败   >=0:实际读取个数
     *
     * @author 李凤敏 2014.05.26
     */
    public int ReadTry(T SubBuf[], int iLen)
    {
        int iCurSize;
        int iReadSize;

        if ((null == m_pBuff) || (null == SubBuf))
        {
            //Log.e("FxMonitor", "m_pBuff or pBuff parameter is 0.");
            return -1;
        }

        if (Empty() == true)
        {
            //Log.w("FxMonitor", "BufQueue is empty.");
            return 0;
        }

        iCurSize = Size();
        iReadSize = (iCurSize < iLen) ? iCurSize : iLen;

        Object subArr[] = m_pBuff.toArray();
        if (((m_iHead + iReadSize) <= m_iCapacity))
        {
            System.arraycopy(subArr, m_iHead % m_iCapacity, SubBuf, 0, iReadSize);
        }
        else  // 如果是数据是越过数组尾部，拐弯回到数组开始处，这样要分两段来取数据
        {
            int iFirstPart = m_iCapacity - m_iHead;
            System.arraycopy(subArr, m_iHead, SubBuf, 0, iFirstPart);
            System.arraycopy(subArr, 0, SubBuf, iFirstPart, iReadSize - iFirstPart);
        }

        return iReadSize;
    }


    /**
     * 队列里写一组数据
     *
     * @param SubBuf : 待写入的数据缓冲区
     * @param iLen  : 长度
     *
     * @return 写入数据是否成功/写入个数 -1:写入失败   >=0:实际写入个数
     *
     * @author 李凤敏 2014.05.26
     */
    public int Write(T SubBuf[], int iLen)
    {
        int iFreeSize;

        if ((null == m_pBuff) || (null == SubBuf))
        {
            //Log.e("FxMonitor", "m_pBuff or pBuff parameter is 0.");
            return -1;
        }

        iFreeSize = m_iCapacity - Size() - 1;  //通过减1的方式实现队列的满空判断

        // 计算可写的数据量
        iFreeSize = (iFreeSize >= iLen) ? iLen : iFreeSize;

        // 如果没有超过缓冲区数组下限， 直接拷贝
        if ((m_iTail + iFreeSize) < m_iCapacity)
        {
            //memcpy( m_pBuff + m_iTail , pBuff, iFreeSize * sizeof(T));

            for (int i = 0; i < iFreeSize; i++)
            {
                if (m_pBuff.size() < m_iCapacity)
                {
                    m_pBuff.add(SubBuf[i]);
                }
                else
                {
                    m_pBuff.set(m_iTail, SubBuf[i]);
                }

                m_iTail = (m_iTail + 1) % m_iCapacity;
            }
        }
        else  //否则分两部分拷贝
        {
            int iFirstPart = m_iCapacity - m_iTail;

            //memcpy(m_pBuff + m_iTail, pBuff, iFirstPart * sizeof(T));

            for (int i = 0; i < iFirstPart; i++)
            {
                if (m_pBuff.size() < m_iCapacity)
                {
                    m_pBuff.add(SubBuf[i]);
                }
                else
                {
                    m_pBuff.set(m_iTail, SubBuf[i]);
                }

                m_iTail = (m_iTail + 1) % m_iCapacity;
            }

            for (int i = iFirstPart; i < iFreeSize; i++)
            {
                if (m_pBuff.size() < m_iCapacity)
                {
                    m_pBuff.add(SubBuf[i]);
                }
                else
                {
                    m_pBuff.set(m_iTail, SubBuf[i]);
                }

                m_iTail = (m_iTail + 1) % m_iCapacity;
            }

            //memcpy(m_pBuff, pBuff + iFirstPart, (iFreeSize - iFirstPart) * sizeof(T));
        }

        //m_iTail = (m_iTail + iFreeSize) % m_iCapacity;

        return iFreeSize;
    }


    /**
     * 判断队列是否已满
     *
     * @param
     *
     * @return true : 队列满    false : 未满
     *
     * @author 李凤敏 2014.05.26
     */
    public Boolean Full()
    {
        if (0 == m_iCapacity)
        {
            return false;
        }
        else
        {
            return ((((m_iTail + 1) % m_iCapacity) == m_iHead) ? true : false);
        }
    }

    /**
     * 判断队列是否为空
     *
     * @param
     *
     * @return true : 队列空   false : 队列非空
     *
     * @author 李凤敏 2014.05.26
     */
    public Boolean Empty()
    {
        return ((m_iHead == m_iTail) ? true : false);
    }


    /**
     * 返回队列的长度
     *
     * @param
     *
     * @return 队列目前长度
     *
     * @author 李凤敏 2014.05.26
     */
    public int Size()
    {
        if (0 == m_iCapacity)
        {
            return 0;
        }
        else
        {
            return (m_iTail + m_iCapacity - m_iHead) % m_iCapacity;
        }
    }

    /**
     * 重新设定大小
     *
     * @param iCap: 队列大小
     *
     * @return 重设队列大小是否成功， true:重设队列大小成功  false:重设队列大小失败
     *
     * @author 李凤敏 2014.05.26
     */
    public Boolean SetCapacity(int iCap)
    {
        if (iCap <= 0)
        {
            return false;
        }

        m_pBuff = new ArrayList<T>(iCap);
        m_iCapacity = iCap;
        m_iHead = 0;
        m_iTail = 0;

        if (m_pBuff == null)
        {
            //Log.e("FxMonitor", "BufQueue m_pBuff new failed.");
        }

        return true;
    }

    /**
     * 获取缓冲区的容量大小
     *
     * @param
     *
     * @return 缓冲区的容量大小
     *
     * @author 李凤敏 2014.06.05
     */
    public int getCapacity()
    {
        return m_iCapacity;
    }


    /**
     * 清空队列

     *
     * @return
     *
     * @author 李凤敏 2014.05.26
     */
    public void Clear()
    {
        m_iHead = 0;
        m_iTail = 0;
        m_pBuff.clear();
    }

    /**
     * 重载=
     * @author 李凤敏 2014.05.26
     * @param  rhs， 被赋值的队列
     * @return 赋值后的队列
     */
	/*BufQueue<T> &operator=(const BufQueue<T> &rhs)
	{
		if (this == &rhs)
		{
			return *this;
		}

		if (m_iCapacity != rhs.m_iCapacity)
		{
			this->SetCapacity(rhs.m_iCapacity);
		}

		m_iHead = rhs.m_iHead;
		m_iTail = rhs.m_iTail;

		memcpy(m_pBuff, rhs.m_pBuff, m_iCapacity * sizeof(T));

		return *this;
	}*/

}
