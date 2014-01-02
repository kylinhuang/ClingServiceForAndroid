手机控制TV端    	TV端service Demo
问题总结
1.键值适配  
new Instrumentation().sendKeyDownUpSync(code);
使系统进行处理

2.连续按同一按键，只执行一次
处理：SwitchPower setTarget（）中                String targetOldValue = target;
每次更改如         String targetOldValue = "NO";

Activity propertyChange()中
arg0.getPropertyName()分别为target，status，_EventedStateVariables，Status

