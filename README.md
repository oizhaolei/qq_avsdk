
AV_Android_SDK

基于互动直播ILVB 1.5版本开发

该sample用于演示音视频通讯类的应用场景

修改Util.java
    public static int APP_ID_TEXT = 1400002371;
    public static String UID_TYPE = "1317";


建立文件app/src/main/res/values/arrays.xml，使用tls生成sig

<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string-array name="openid_list">
        <item>u_139955</item>
        <item>u_139956</item>
        <item>u_139957</item>
    </string-array>
    <string-array name="openkey_list">
        <item>eJx10E9PgzAABfA7n6LpdcbQlgp4w27RKiMS559xaXB0WzNaEArOGb*7CRD77r*yUveZ8OAAAu4ofzfLWqOmOF-aglBJcAIoJ8ePbb17UqRG4FaYqf3nOPwcRHIyX3tWqkyNdWNoPCNMRHNiKqkMaqtTqBTiAShpSORFvsxDD3-06rNkM5ny0ZT6fmJUir3viZvCvp0u7YTcwWNQtmG837jJCLjrveXL9OdcS3UZLwlPVP9Np9zoKub2-9*xiTfYkP73p7hcvkLZrkdnJ4LOVo0ip9Osajroc99OeeXjatqswAoPPlfAMT0Fye</item>
        <item>eJx10M1PgzAYx-E7f0XDFWOgL9TutiHERo1OcIRTw0unTTNWoWMsxv-ZfIZc-1*0l*yfPtAADc7Cm9Let6f2itsCcjXbAAboAC6t78d2NUI0orUNf8deyfDyIazJQcjeqkKLdWdpOChMEzmxHVyNaqrbqAgwgQYyScib7RYpq7vtOrjyk*x0XE11EzDDzH6KjpQxLGLMe0IjEtUz8znNUvxNusVl*eV0TtkX8uH0eiqmT5XuXZGm7uMl1EPjc9ruzutRrfUg01OVmDeXI-m7Rqd3kMJj6GIcFoVgfZ9WrfTsB1fpxfindbjw__</item>
        <item>eJx1kE9PgzAchu98CsJ1xkGhY5h4mHOZrGJU8A*nhpSiP2GsLWV3F1xiL77X50me5P2ybNt2stv0vGBs07ea6r3gjn1hO57vhc7ZHxcCSlpo6qvylwfuccgPPcPiOwGK06LSXA0WwhE6aoYCJW81VHASeur5UYTNVFfWdMj93*ngbYDJ4mke39QP6Yi-kqDuslzWL1K6yV7iBjMcC5k*jgi-z1hP*tV0BovZuFneHa4xnhOpJytyWMfvn2r5geCKiaQZu36Odl6B4ucqvzSSGtanYwLsBtPQRRODbrnqYNMOgmN9Wz8631y5</item>
    </string-array>
</resources>