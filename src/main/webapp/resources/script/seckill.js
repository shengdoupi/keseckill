//存放主要交互逻辑js代码

//javascript 模块化
//seckill.detail.init(params);
var seckill = {
    //封装秒杀相关ajax的url

    URL: {
        now: function(){
            return '/seckill/time/now';

        },
        exposer : function(seckillId){
            return '/seckill/' + seckillId +'//exposer';
        },
        execution : function(seckillId, md5){
            return '/seckill/' + seckillId + '/' + md5 +'/execution';
        }
    },
    handleSeckillkill : function(seckillId, node){
        //处理秒杀逻辑/获取秒杀接口地址，控制显示逻辑，用户执行秒杀
        //先隐藏，因为要先获取秒杀地址
        node.hide()
            .html('<button class="btn btn-primary btn-lg" id="killBtn"> 开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId), {}, function(result){
            //在回调函数中，执行交互流程
            if(result && result['success']){
                var exposer = result['data'];
                if(exposer['exposed']){
                    //开启秒杀
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId,md5);
                    console.log("killUrl:"+killUrl);
                    //只绑定一次点击事件
                    $('#killBtn').one('click',function (){
                        //绑定的是执行秒杀请求的操作
                        //1:先禁用按钮
                        $(this).addClass('disabled');
                        //2:发送秒杀请求执行秒杀

                        $.post(killUrl,{},function(result){
                            if(result && result['success']){
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                //显示秒杀结果
                                node.html('<span class="label label-success">'+stateInfo +'</span>');
                            }
                        });
                    });
                    node.show();
                }else{
                    //未开启秒杀,客户端时间偏差，让客户端重新计算计时逻辑
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    seckill.countdown(seckillId, now,start,end);
                }

            }else{
                console.log('result:'+result);
            }

        });

    },
    //验证手机号
    validatePhone: function(phone){
        if(phone && phone.length==11 && !isNaN(phone)){
            return true;
        }else{
            return false;
        }
    },
    countdown:function(seckillId, nowTime, startTime, endTime){
        //#是jquery的id选择器
        var seckillBox = $('#seckill-box');
        //时间判断
        if(nowTime > endTime){
            //miaoshajieshu
            seckillBox.html('秒杀结束！');
        }else if(nowTime < startTime){
            //秒杀未开始
            //需要计时
            var killTime = new Date(startTime + 1000);

            seckillBox.countdown(killTime,function(event){
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                seckillBox.html(format);
                //时间完整后回调时间
            }).on('finish.countdown', function (){
                //获取秒杀接口地址，控制显示逻辑，用户执行秒杀
                seckill.handleSeckillkill(seckillId,seckillBox);
            });

        }else{
            //秒杀开始
            seckill.handleSeckillkill(seckillId,seckillBox);
        }
},
    //详情页秒杀逻辑
    detail:{
        //详情页初始化
        init : function(params){
            //手机验证和登录，计时交互
            //规划交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('killPhone');

            //验证手机号
            if(!seckill.validatePhone(killPhone)){
                //绑定手机号phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show:true,//显示弹出层
                    backdrop:'static', //禁止位置关闭
                    keyboard: false//关闭键盘事件
                });
                $('#killPhoneBtn').click(function(){
                   var inputPhone = $('#killPhoneKey').val();
                   console.log('inputPhone='+inputPhone);//TODO
                   if(seckill.validatePhone(inputPhone)){
                       //电话写入cookie
                       $.cookie('killPhone',inputPhone,{expires:7,path:'/seckill'});
                       //刷新页面
                       window.location.reload();
                   }else{
                       $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                   }
                });
            }
            //已经登录
            //开始计时
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                if(result && result['success']){
                    var nowTime = result['data'];
                    //时间判断
                   seckill.countdown(seckillId,nowTime,startTime,endTime);

                }else{
                    console.log('result:'+result);
                }
            });
        }
    }
}