namespace java cache.api

struct Value{
	1: string val
	2: string clsName
	}

service CacheAPI {

    void write(1:string key, 2:Value value)
	
	void remove(1:string key)
		
	map<string, Value> readAll()
	
	Value read(1:string key)
	
}