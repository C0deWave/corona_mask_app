package com.jumyeong.corona

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

//ClusterRenderer 클래스는 마커를 다시 엔더링 하는 작업을 담당하는 클래스 입니다.
class ClusterRenderer(context: Context?, map : GoogleMap?, clusterManager: ClusterManager<MyItem>?):
        DefaultClusterRenderer<MyItem>(context,map,clusterManager){
    init {
        //전달받은 clusterManager객체에 renderer을 자신으로 지정한다.
        clusterManager?.renderer = this
    }

    //클러스터 아이템이 렌더링 되기전 호출되는 함수이다.
    override fun onBeforeClusterItemRendered(item: MyItem?, markerOptions: MarkerOptions?) {
        //마커의 아이콘 지정
        markerOptions?.icon(item?.getIcon())
        markerOptions?.visible(true)
    }
}