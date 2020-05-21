package com.jumyeong.corona

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_bar.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    // 런타임에서 권한이 필요한 퍼미션 목록
    val PERMISSION = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    //퍼미션 승인 요청시 사용하는 요청 코드
    val REQUEST_PERMISSION_CODE = 1

    //기본맵 줌 레벨
    val DEFAULT_ZOOM_LEVEL = 17f

    //현재 위치를 가져올 수 없는 경우 서울 시청의 위치를 지도로 보여주기 위해서 서울 시청의 위치를 변수로 선언
    //LatLng클래스는 위도와 경도를 가지는 클래스이다.
    val CITY_HALL = LatLng(37.5662952, 126.97794509999994)

    //구글맵 객체를 참조할 멤버 변수
    var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //맵뮤에 onCreate함수 호출
        mapView.onCreate(savedInstanceState)

        //드롭 다운 투명화
        searchBar.autoCompleteTextView.setDropDownBackgroundResource(R.color.none)

        //앱이 실행될 때 권한이 있는지 체크하는 함수
        if (hasPermissions()) {
            //권한이 있는 경우 맵 초기화
            initMap()
        } else {
            //권한이 없는 경우 권한을 요청한다.
            ActivityCompat.requestPermissions(this, PERMISSION, REQUEST_PERMISSION_CODE)
        }

        //현재위치 클릭 버튼 이벤트 리스너 설정
        myLocationButton.setOnClickListener { onMyLocationButtonClick() }

    }

    //퍼미션 확인하는 함
    private fun hasPermissions(): Boolean {
        for (permission in PERMISSION) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    //ClusterManager 변수 선언
    var clusterManager: ClusterManager<MyItem>? = null

    //
    // ClusterRenderer 변수 선언
    var clusterRenderer: ClusterRenderer? = null

    //맵 초기화 하는 함수
    @SuppressLint("MissingPermission")
    fun initMap() {
        //맵 뷰에서 구글 맵을 불러오는 함수, 컬백 함수에서 구글 맵 객체가 전달된다.
        mapView.getMapAsync {

            //cluster객체 초기화
            clusterManager = ClusterManager(this, it)
            clusterRenderer = ClusterRenderer(this, it, clusterManager)

            //OnCameraIdleListener 와 OnMarkerClickListener 을 클러스트 매니저로 지정한다.
            it.setOnCameraIdleListener(clusterManager)
            it.setOnMarkerClickListener(clusterManager)

            //구글맵 멤버 변수에 구글맵 객체 저장
            googleMap = it
            //현재 위치로 이동 버튼 비활성화
            it.uiSettings.isMyLocationButtonEnabled = false

            //위치 사용 권한이 있는 경우
            when {
                hasPermissions() -> {
                    //현재 위치 표시 활성화
                    it.isMyLocationEnabled = true
                    //현재 위치로 카메라 이동
                    it.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            getMyLocation(),
                            DEFAULT_ZOOM_LEVEL
                        )
                    )
                }
                else -> {
                    //권한이 없으면 서울시청의 위치로 이동
                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(CITY_HALL, DEFAULT_ZOOM_LEVEL))
                }
            }
        }
    }

    //내 위치를 구하는 함수
    @SuppressLint("MissingPermission")
    fun getMyLocation(): LatLng {
        //위치를 측정하는 프로바이더를 GPS센서로 측정한다.
        val locationProvider: String = LocationManager.GPS_PROVIDER
        //위치 서비스 객체를 불러온다.
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //마지막으로 업데이트 된 위치를 가져온다.
        val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider)
        //위도 경도 객체로 반환한다.
        return LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
    }

    //버튼을 눌렀을때 자신의 위치로 이동하는 함수이다.
    fun onMyLocationButtonClick() {
        when {
            hasPermissions() -> {
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        getMyLocation(),
                        DEFAULT_ZOOM_LEVEL
                    )
                )
            }
            else -> Toast.makeText(applicationContext, "위치사용권한 설정을 동의해 주세요", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //하단 맵뷰의 라이프 사이클 함수 호출을 위한 코드들이다.
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        initMap()
    }

    // 마스크는 api키 값이 필요가 없다.
    //val API_KEY = ""
    //앱이 비활성화 될때 백그라운드 작업도 취소하기 위한 변수 선언
    var task: TolietReadTask? = null

    //마스크 정보 집합을 저장할 array변수 검색을 위해 저장
    var mask = JSONArray()

    //JsonObject를 키로 MyItem객체를  저장할 맵
    val itemMap = mutableMapOf<JSONObject, MyItem>()

    //마스크 이미지로 사용할 bitmap
    val bitmapAvailable by lazy {
        //이미지는 나중에 바꿔준다.
        val drawable = resources.getDrawable(R.drawable.mask_sign, theme) as BitmapDrawable
        Bitmap.createScaledBitmap(drawable.bitmap, 64, 64, false)
    }
    val bitmapDisabailable by lazy {
        //마스크가 없는 경우이다.
        val drawable = resources.getDrawable(R.drawable.mask_unsign, theme) as BitmapDrawable
        Bitmap.createScaledBitmap(drawable.bitmap, 64, 64, false)
    }

    //JSONArray를 발생시키기 위해 확장함수 사용
    fun JSONArray.merge(anotherArray: JSONArray) {
        for (i in 0 until anotherArray.length()) {
            this.put(anotherArray.get(i))
        }
    }

    //    //화장실 정보를 읽어와 JSONArray로 변환하는 함수
    fun readData(): JSONObject {
//        // 위도와 경도를 잘 잡아줘야 한다.
//        //나중에 주성 바람 위도 경도 5키로 이내이다..
        var lat = getMyLocation().latitude;
        var lut = getMyLocation().longitude;
        val url =
            URL("https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?lat=${lat}&lng=${lut}&m=5000")
        val connection = url.openConnection()
        val data = connection.getInputStream().readBytes().toString(charset("UTF-8"))
        return JSONObject(data)
    }

    //화장실 데이터를 읽어오는 AsyncTask
    inner class TolietReadTask : AsyncTask<Void, JSONArray, String>() {
        override fun onPreExecute() {
            //구글맵 마커 초기화
            googleMap?.clear()
            //마스크 정보 초기화
            mask = JSONArray()
            //initMap 변수 초기화
            itemMap.clear()
        }

        override fun doInBackground(vararg params: Void?): String {

            //json 데이터를 읽어들인다.
            val jsonObject = readData()

            //화장실 정보 데이터 집합을 가져온다.
            val rows = jsonObject.getJSONArray("stores")

            //기존에 읽은 데이터와 병합
            mask.merge(rows)

            //UI업데이트를 위해 progressa 발행
            publishProgress(rows)
            return "complete"
        }

        override fun onProgressUpdate(vararg values: JSONArray?) {
            //vararg는 JSONArray파라미터를 가변적으로 전달하도록 하는 키워드이다.
            //인덱스 0의 데이터를 사용한다.
            val array = values[0]
            array?.let {
                for (i in 0 until array.length()) {
                    addMarkers(array.getJSONObject(i))
                }
            }
            //클러스터 매니저의 클러스터링 실행
            clusterManager?.cluster()
        }

        override fun onPostExecute(result: String?) {
            // 자동완성 텍스트뷰(AutoCompleteTextView) 에서 사용할 텍스트 리스트
            val textList = mutableListOf<SearchItem>()

            // 모든 화장실의 이름을 텍스트 리스트에 추가
            for (i in 0 until mask.length()) {
                val maskName = mask.getJSONObject(i)
                textList.add(
                    SearchItem(
                        maskName.getString("name"),
                        howManyMask(maskName.optString("remain_stat"))
                    )
                )
            }
            // 자동완성 텍스트뷰에서 사용하는 어댑터 추가
            // simple_dropdown_item_1line 는 검색 바 아래에 나오는 자동완성 리소스 이미지이다.
//            val adapter = ArrayAdapter<SearchItem>(
//                this@MainActivity,
//                android.R.layout.simple_dropdown_item_1line, textList
//            )
            //새로 어댑터를 만듬
            val adapter:AutoCompleteSearchItem = AutoCompleteSearchItem(this@MainActivity,
                textList as ArrayList<SearchItem>
            )

            // 자동완성이 시작되는 글자수 지정
            searchBar.autoCompleteTextView.threshold = 1
            // autoCompleteTextView 의 어댑터를 상단에서 만든 어댑터로 지정
            searchBar.autoCompleteTextView.setAdapter(adapter)
            //여기까지하면 검색 바에 리스트만 추가가 되었다.
        }
    }

    fun JSONArray.findByChildProperty(propertyName: String, value: String): JSONObject? {
        // JSONArray 를 순회하면서 각 JSONObject 의 프로퍼티의 값이 같은지 확인
        for (i in 0 until length()) {
            val obj = getJSONObject(i)
            if (value == obj.getString(propertyName)) return obj
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        task?.cancel(true)
        task = TolietReadTask()
        task?.execute()

        //서치바 아이콘의 검색 아이콘의 이벤트 리스너를 지정한다.
        searchBar.imageView.setOnClickListener {
            //오토 컴플리트의 텍스트를 읽어서 키워드로 가져온다.
            val keyword = searchBar.autoCompleteTextView.text.toString()
            //키워드 값이 없으면 그대로 리턴한다.
            if (TextUtils.isEmpty(keyword)) return@setOnClickListener

            //검색 키워드에 맞는 JSONObject를 찾는다.
            mask.findByChildProperty("name", keyword)?.let {
                //itemMap에서 JSONObject를 키오 가진 MyItem 객체를 가져온다.
                val myItem = itemMap[it]

                //마커의 위치로 맵의 카메라를 이동한다.
                googleMap?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.getDouble("lat"), it.getDouble("lng")), DEFAULT_ZOOM_LEVEL
                    )
                )
                clusterManager?.cluster()

                //클러스터 매니저에서 myItem을 기반으로 하는 마커를 검색한다.
                //myItem은 위도 경도 제목 설명 속성이 같으면 같은 객체로 취급한다.
                val marker = clusterRenderer?.getMarker(myItem)
                //마커가 인포 윈도우를 보여준다.
                //마크를 누른 효과를 준다.
                marker?.showInfoWindow()
            }

            //검색 텍스트뷰의 텍스트를 지운다.
            searchBar.autoCompleteTextView.setText("")
        }
    }

    override fun onStop() {
        super.onStop()
        task?.cancel(true)
        task = null
    }

    fun addMarkers(mask: JSONObject) {

        val item = MyItem(
            LatLng(mask.getDouble("lat"), mask.getDouble("lng")),
            mask.getString("name"),
            mask.optString("remain_stat"),
            maskImage(mask.optString("remain_stat"))
        )
        //clusterManager을 통해서 마커를 추가한다.
        clusterManager?.addItem(
            MyItem(
                LatLng(mask.getDouble("lat"), mask.getDouble("lng")),
                mask.getString("name"),
//                (result),
                howManyMask(mask.optString("remain_stat")),
                maskImage(mask.optString("remain_stat"))
            )
        )
        //검색을 담당하는 itemMap에 마스크 객체를 키로 해서 MyItem을 저장한다.
        itemMap.put(mask, item)
        Log.d("마크를 찍었습니다.", "마크")
    }


    // 알맞은 마스크 이미지를 반환해주는 함수
    fun maskImage(availMask: String?): BitmapDescriptor {
        if (availMask == "break" || availMask == null)
            return BitmapDescriptorFactory.fromBitmap(bitmapDisabailable)
        else
            return BitmapDescriptorFactory.fromBitmap(bitmapAvailable)
    }


    // 스니펫을 적절한 설명으로 바꾸어주는 함수
    fun howManyMask(str: String): String {
        when (str) {
            "plenty" -> return "100개 이상"
            "some" -> return "30개 ~ 100개"
            "few" -> return "2개 ~ 30개"
            "empty" -> return "1개 이하"
            "break" -> return "판매 중지"
            else -> return "정보 없음"
        }
    }


}
