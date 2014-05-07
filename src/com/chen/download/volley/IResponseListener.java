package com.chen.download.volley;

public interface IResponseListener
{
	void onReqStart();

	void onSuccess(Object o);

	void onFailure(Object o);

	void onFinish();
}
