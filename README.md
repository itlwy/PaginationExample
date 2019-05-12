# PaginationExample
仿网页风格分页器，和RecycleView封装一起配合使用，也可单独使用，喜欢就star、fork下吧～谢谢

## 目录

* [功能介绍](#功能介绍)
* [效果图](#效果图)
* [如何引入](#如何引入)
* [简单使用](#简单使用)
* [依赖](#依赖)
* [License](#License)

## 功能介绍

- [x] 仿网页分页器效果

## 效果图

![示例1](https://raw.githubusercontent.com/itlwy/PaginationExample/master/resources/pic1.png)
![示例2](https://raw.githubusercontent.com/itlwy/PaginationExample/master/resources/pic2.png)

# Screenshots
#![Alt text](https://github.com/itlwy/PaginationExample/blob/master/resources/pagination.gif)

## 如何引入
### Gradle引入
### step 1
Add the JitPack repository to your build file

```
	allprojects {
			repositories {
				...
				maven { url 'https://jitpack.io' }
			}
		}
```

### Step 2
Add the dependency

```
dependencies {
	         implementation 'com.github.itlwy:PaginationExample:0.0.16'
	}

```

## 依赖
- recyclerview : com.android.support:recyclerview-v7:28.0.0


## License

   	Copyright 2019 lwy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.