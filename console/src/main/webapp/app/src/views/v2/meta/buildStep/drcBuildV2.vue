<template v-if="current === 0" :key="0">
  <div>
    <Alert :type="status" show-icon v-if="hasResp" style="width: 65%; margin-left: 250px">
      {{title}}
      <span slot="desc" v-html="message"></span>
    </Alert>
    <Row>
      <i-col span="12">
        <Form ref="drc1" :model="srcBuildParam" :rules="ruleDrc" :label-width="250"
              style="float: left; margin-top: 50px">
          <FormItem label="源集群名" prop="srcMhaName" style="width: 600px">
            <Input v-model="srcBuildParam.mhaName" @input="changeSrcMha" placeholder="请输入源集群名"/>
          </FormItem>
          <FormItem label="选择Replicator" prop="replicator">
            <Select v-model="srcBuildParam.replicatorIps" multiple style="width: 250px" placeholder="选择源集群Replicator">
              <Option v-for="item in replicatorList.src" :value="item.ip" :key="item.ip">{{ item.ip }} —— {{ item.az
                }}
              </Option>
            </Select>
            &nbsp;
            <Button type="success" @click="autoConfigSrcReplicator">自动录入</Button>
          </FormItem>
          <FormItem v-if="showMhaApplierConfig(true)" label="选择Applier" prop="applier">
            <Select v-model="srcBuildParam.applierIps" multiple style="width: 250px" placeholder="选择源集群Applier">
              <Option v-for="item in applierList.src" :value="item.ip" :key="item.ip">{{ item.ip }} —— {{ item.az }}
              </Option>
            </Select>
            &nbsp;
            <Button type="success" @click="autoConfigSrcApplier">自动录入</Button>
          </FormItem>
          <FormItem label="初始拉取位点R" style="width: 600px">
            <Input v-model="srcBuildParam.replicatorInitGtid" placeholder="变更replicator机器时,请输入binlog拉取位点"/>
            <Row>
              <Col span="12">
                <Button @click="querySrcMhaMachineGtid">查询mha位点</Button>
                <span v-if="hasTest1">
                  <Icon :type="testSuccess1 ? 'ios-checkmark-circle' : 'ios-close-circle'"
                        :color="testSuccess1 ? 'green' : 'red'"/>
                    {{ testSuccess1 ? '查询实时位点成功' : '连接查询失败' }}
                </span>
              </Col>
              <Col span="12">
                <Button type="success" @click="querySrcMhaGtidCheckRes">位点校验</Button>
              </Col>
            </Row>
          </FormItem>
          <FormItem v-if="showMhaApplierConfig(true)" label="初始同步位点A" style="width: 600px">
            <Input v-model="srcBuildParam.applierInitGtid" placeholder="请输入DRC同步起始位点"/>
          </FormItem>
          <FormItem label="同步配置" style="width: 600px">
            <Button type="primary" ghost @click="getSrcDbReplications">同步表管理</Button>
          </FormItem>
          <FormItem v-if="showDbMhaApplierConfig(true)" label="DB Applier 配置"  style="width: 600px">
            <Button type="primary" ghost @click="getSrcDbApplierReplications">Db Applier 管理</Button>
          </FormItem>
        </Form>
      </i-col>
      <i-col span="12">
        <Form ref="drc1" :model="dstBuildParam" :rules="ruleDrc" :label-width="250"
              style="float: left; margin-top: 50px">
          <FormItem label="目标集群名" prop="dstMhaName" style="width: 600px">
            <Input v-model="dstBuildParam.mhaName" @input="changeDstMha" placeholder="请输入目标集群名"/>
          </FormItem>
          <FormItem label="选择Replicator" prop="replicator">
            <Select v-model="dstBuildParam.replicatorIps" multiple style="width: 250px" placeholder="选择目标集群Replicator">
              <Option v-for="item in replicatorList.dst" :value="item.ip" :key="item.ip">{{ item.ip }} —— {{ item.az
                }}
              </Option>
            </Select>
            &nbsp;
            <Button type="success" @click="autoConfigDstReplicator">自动录入</Button>
          </FormItem>
          <FormItem v-if="showMhaApplierConfig(false)" label="选择Applier" prop="applier">
            <Select v-model="dstBuildParam.applierIps" multiple style="width: 250px" placeholder="选择目标集群Applier">
              <Option v-for="item in applierList.dst" :value="item.ip" :key="item.ip">{{ item.ip }} —— {{ item.az }}
              </Option>
            </Select>
            &nbsp;
            <Button type="success" @click="autoConfigDstApplier">自动录入</Button>
          </FormItem>
          <FormItem label="初始拉取位点R" style="width: 600px">
            <Input v-model="dstBuildParam.replicatorInitGtid" placeholder="变更replicator机器时,请输入binlog拉取位点"/>
            <Row>
              <Col span="12">
                <Button @click="queryDstMhaMachineGtid">查询mha位点</Button>
                <span v-if="hasTest2">
                  <Icon :type="testSuccess2 ? 'ios-checkmark-circle' : 'ios-close-circle'"
                        :color="testSuccess2 ? 'green' : 'red'"/>
                    {{ testSuccess2 ? '查询实时位点成功' : '连接查询失败' }}
                </span>
              </Col>
              <Col span="12">
                <Button type="success" @click="queryDstMhaGtidCheckRes">位点校验</Button>
              </Col>
            </Row>
          </FormItem>
          <FormItem v-if="showMhaApplierConfig(false)" label="初始同步位点A" style="width: 600px">
            <Input v-model="dstBuildParam.applierInitGtid" placeholder="请输入DRC同步起始位点"/>
          </FormItem>
          <FormItem label="同步配置" style="width: 600px">
            <Button type="primary" ghost @click="getDstDbReplications">同步表管理</Button>
          </FormItem>
          <FormItem v-if="showDbMhaApplierConfig(false)" label="DB Applier 配置" style="width: 600px">
            <Button type="primary" ghost @click="getDstDbApplierReplications">Db Applier 管理</Button>
          </FormItem>
        </Form>
      </i-col>
    </Row>
    <Form :label-width="250" style="margin-top: 50px">
      <FormItem>
        <br>
        <Button type="primary" style="width: 100px" @click="preCheckConfigure ()">提交</Button>
      </FormItem>
      <Modal
        v-model="reviewModal"
        title="确认配置信息"
        width="900px"
        @on-ok="submitConfig">
        <Row :gutter="5">
          <i-col span="12">
            <Form style="width: 80%">
              <FormItem label="源集群名">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="srcBuildParam.mhaName" readonly/>
              </FormItem>
              <FormItem label="源集群端Replicator">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="srcBuildParam.replicatorIps"
                       readonly/>
              </FormItem>
              <FormItem label="源集群端Applier">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="srcBuildParam.applierIps"
                       readonly/>
              </FormItem>
              <FormItem label="源集群端R位点">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="srcBuildParam.replicatorInitGtid"
                       readonly/>
              </FormItem>
              <FormItem label="源集群端A位点">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="srcBuildParam.applierInitGtid"
                       readonly/>
              </FormItem>
            </Form>
          </i-col>
          <i-col span="12">
            <Form style="width: 80%">
              <FormItem label="目标集群名">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="dstBuildParam.mhaName" readonly/>
              </FormItem>
              <FormItem label="目标集群端Replicator">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="dstBuildParam.replicatorIps"
                       readonly/>
              </FormItem>
              <FormItem label="目标集群端Applier">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="dstBuildParam.applierIps"
                       readonly/>
              </FormItem>
              <FormItem label="目标集群端R位点">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="dstBuildParam.replicatorInitGtid"
                       readonly/>
              </FormItem>
              <FormItem label="目标集群端A位点">
                <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="dstBuildParam.applierInitGtid"
                       readonly/>
              </FormItem>
            </Form>
          </i-col>
        </Row>
      </Modal>
      <Modal
        v-model="resultModal"
        title="配置结果"
        width="1200px">
        <Form style="width: 100%">
          <FormItem label="集群配置">
            <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="result" readonly/>
          </FormItem>
        </Form>
      </Modal>
      <Modal
        v-model="gtidCheck.modal"
        title="gitd位点校验结果"
        width="900px">
        <Form style="width: 80%">
          <FormItem label="校验结果">
            <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="gtidCheck.resVo.legal" readonly/>
          </FormItem>
          <FormItem label="当前Mha">
            <Input :autosize="{minRows: 1,maxRows: 30}" v-model="gtidCheck.resVo.mha" readonly/>
          </FormItem>
          <FormItem label="配置位点">
            <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="gtidCheck.resVo.configGtid" readonly/>
          </FormItem>
          <FormItem label="purgedGtid">
            <Input type="textarea" :autosize="{minRows: 1,maxRows: 30}" v-model="gtidCheck.resVo.purgedGtid" readonly/>
          </FormItem>
        </Form>
      </Modal>
    </Form>
  </div>
</template>
<script>
export default {
  name: 'drcBuildV2',
  props: {
    srcMhaName: String,
    dstMhaName: String,
    srcDc: String,
    dstDc: String,
    env: String
  },
  data () {
    return {
      result: '',
      status: '',
      title: '',
      message: '',
      hasResp: false,
      hasTest1: false,
      testSuccess1: false,
      hasTest2: false,
      testSuccess2: false,
      reviewModal: false,
      resultModal: false,
      replicatorList: {
        src: [],
        dst: []
      },
      applierList: {
        src: [],
        dst: []
      },
      srcBuildParam: {
        mhaName: this.srcMhaName,
        replicatorIps: [],
        applierIps: [],
        replicatorInitGtid: '',
        applierInitGtid: ''
      },
      srcBuildData: {
        dbApplierDtos: [],
        dbApplierSwitch: false
      },
      dstBuildParam: {
        mhaName: this.dstMhaName,
        replicatorIps: [],
        applierIps: [],
        replicatorInitGtid: '',
        applierInitGtid: ''
      },
      dstBuildData: {
        dbApplierDtos: [],
        dbApplierSwitch: false
      },
      gtidCheck: {
        modal: false,
        resVo: {
          mha: '',
          legal: '',
          configGtid: '',
          purgedGtid: ''
        }
      },
      ruleDrc: {
        oldClusterName: [
          { required: true, message: '源集群名不能为空', trigger: 'blur' }
        ],
        newClusterName: [
          { required: true, message: '新集群名不能为空', trigger: 'blur' }
        ],
        env: [
          { required: true, message: '环境不能为空', trigger: 'blur' }
        ],
        replicator: [
          { required: true, message: 'replicator不能为空', trigger: 'blur' }
        ],
        applier: [
          { required: true, message: 'applier不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dataWithPage () {
      const data = this.nameFilterCheck.tableData
      const start = this.nameFilterCheck.current * this.nameFilterCheck.size - this.nameFilterCheck.size
      const end = start + this.nameFilterCheck.size
      return [...data].slice(start, end)
    }
  },
  methods: {
    flattenObj (ob) {
      const result = {}
      for (const i in ob) {
        if ((typeof ob[i]) === 'object' && !Array.isArray(ob[i])) {
          const temp = this.flattenObj(ob[i])
          for (const j in temp) {
            result[i + '.' + j] = temp[j]
          }
        } else {
          result[i] = ob[i]
        }
      }
      return result
    },
    autoConfigSrcReplicator () {
      // const param = {
      //   mhaName: this.srcBuildParam.mhaName,
      //   type: 0,
      //   selectIps: this.srcBuildParam.replicatorIps
      // }
      // const reqParam = this.flattenObj(param)
      this.axios.get('/api/drc/v2/resource/mha/auto?mhaName=' + this.srcBuildParam.mhaName + '&type=0' + '&selectedIps=' + this.srcBuildParam.replicatorIps).then(response => {
        console.log(response.data)
        this.srcBuildParam.replicatorIps = []
        response.data.data.forEach(ip => this.srcBuildParam.replicatorIps.push(ip.ip))
      })
    },
    autoConfigSrcApplier () {
      this.axios.get('/api/drc/v2/resource/mha/auto?mhaName=' + this.srcBuildParam.mhaName + '&type=1' + '&selectedIps=' + this.srcBuildParam.applierIps)
        .then(response => {
          console.log(response.data)
          this.srcBuildParam.applierIps = []
          response.data.data.forEach(ip => this.srcBuildParam.applierIps.push(ip.ip))
        })
    },
    autoConfigDstReplicator () {
      this.axios.get('/api/drc/v2/resource/mha/auto?mhaName=' + this.dstBuildParam.mhaName + '&type=0' + '&selectedIps=' + this.dstBuildParam.replicatorIps)
        .then(response => {
          console.log(response.data)
          this.dstBuildParam.replicatorIps = []
          response.data.data.forEach(ip => this.dstBuildParam.replicatorIps.push(ip.ip))
        })
    },
    autoConfigDstApplier () {
      this.axios.get('/api/drc/v2/resource/mha/auto?mhaName=' + this.dstBuildParam.mhaName + '&type=1' + '&selectedIps=' + this.dstBuildParam.applierIps)
        .then(response => {
          console.log(response.data)
          this.dstBuildParam.applierIps = []
          response.data.data.forEach(ip => this.dstBuildParam.applierIps.push(ip.ip))
        })
    },
    getSrcMhaResources () {
      this.axios.get('/api/drc/v2/resource/mha/all?mhaName=' + this.srcBuildParam.mhaName + '&type=0')
        .then(response => {
          console.log(response.data)
          this.replicatorList.src = response.data.data
        })
      this.axios.get('/api/drc/v2/resource/mha/all?mhaName=' + this.srcBuildParam.mhaName + '&type=1')
        .then(response => {
          console.log(response.data)
          this.applierList.src = response.data.data
        })
    },
    getDstMhaResources () {
      this.axios.get('/api/drc/v2/resource/mha/all?mhaName=' + this.dstBuildParam.mhaName + '&type=0')
        .then(response => {
          console.log(response.data)
          this.replicatorList.dst = response.data.data
        })
      this.axios.get('/api/drc/v2/resource/mha/all?mhaName=' + this.dstBuildParam.mhaName + '&type=1')
        .then(response => {
          console.log(response.data)
          this.applierList.dst = response.data.data
          // response.data.data.forEach(ip => this.applierList.src.push(ip))
        })
    },
    getSrcMhaReplicatorsInUse () {
      this.axios.get('/api/drc/v2/mha/replicator?mhaName=' + this.srcBuildParam.mhaName)
        .then(response => {
          this.srcBuildParam.replicatorIps = response.data.data
        })
    },
    getDstMhaReplicatorsInUse () {
      this.axios.get('/api/drc/v2/mha/replicator?mhaName=' + this.dstBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.dstBuildParam.replicatorIps = response.data.data
        })
    },
    getSrcMhaAppliersInUse () {
      this.axios.get('/api/drc/v2/config/mha/applier?srcMhaName=' + this.dstBuildParam.mhaName + '&dstMhaName=' + this.srcBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.srcBuildParam.applierIps = response.data.data
        })
      this.axios.get('/api/drc/v2/config/mha/applierGtid?srcMhaName=' + this.dstBuildParam.mhaName + '&dstMhaName=' + this.srcBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.srcBuildParam.applierInitGtid = response.data.data
        })
    },
    getDstMhaAppliersInUse () {
      this.axios.get('/api/drc/v2/config/mha/applier?srcMhaName=' + this.srcBuildParam.mhaName + '&dstMhaName=' + this.dstBuildParam.mhaName)
        .then(response => {
          this.dstBuildParam.applierIps = response.data.data
        })
      this.axios.get('/api/drc/v2/config/mha/applierGtid?srcMhaName=' + this.srcBuildParam.mhaName + '&dstMhaName=' + this.dstBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.dstBuildParam.applierInitGtid = response.data.data
        })
    },
    getMhaDbAppliersInUse () {
      this.axios.get('/api/drc/v2/config/mha/dbApplier?srcMhaName=' + this.dstBuildParam.mhaName + '&dstMhaName=' + this.srcBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.srcBuildData.dbApplierDtos = response.data.data
        })
      this.axios.get('/api/drc/v2/config/mha/dbApplier?srcMhaName=' + this.srcBuildParam.mhaName + '&dstMhaName=' + this.dstBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.dstBuildData.dbApplierDtos = response.data.data
        })
      this.axios.get('/api/drc/v2/config/mha/dbApplier/switch?mhaName=' + this.srcBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.srcBuildData.dbApplierSwitch = response.data.data
        })
      this.axios.get('/api/drc/v2/config/mha/dbApplier/switch?mhaName=' + this.dstBuildParam.mhaName)
        .then(response => {
          console.log(response.data)
          this.dstBuildData.dbApplierSwitch = response.data.data
        })
    },
    querySrcMhaMachineGtid () {
      const that = this
      that.axios.get('/api/drc/v2/mha/gtid/executed?mha=' + this.srcBuildParam.mhaName)
        .then(response => {
          this.hasTest1 = true
          if (response.data.status === 0) {
            this.srcBuildParam.replicatorInitGtid = response.data.data
            this.testSuccess1 = true
          } else {
            this.testSuccess1 = false
          }
        })
    },
    querySrcMhaGtidCheckRes () {
      if (this.srcBuildParam.replicatorInitGtid == null || this.srcBuildParam.replicatorInitGtid === '') {
        alert('位点为空！')
        return
      }
      const that = this
      that.axios.get('/api/drc/v2/mha/gtid/checkResult?mha=' + this.srcBuildParam.mhaName +
        '&configGtid=' + this.srcBuildParam.replicatorInitGtid)
        .then(response => {
          if (response.data.status === 0) {
            this.gtidCheck.resVo = {
              mha: this.srcBuildParam.mhaName,
              legal: response.data.data.legal === true ? '合理位点' : 'binlog已经被purge',
              configGtid: this.srcBuildParam.replicatorInitGtid,
              purgedGtid: response.data.data.purgedGtid
            }
            this.gtidCheck.modal = true
          } else {
            alert('位点校验失败!')
          }
        })
    },
    queryDstMhaMachineGtid () {
      const that = this
      that.axios.get('/api/drc/v2/mha/gtid/executed?mha=' + this.dstBuildParam.mhaName)
        .then(response => {
          this.hasTest2 = true
          if (response.data.status === 0) {
            this.dstBuildParam.replicatorInitGtid = response.data.data
            this.testSuccess2 = true
          } else {
            this.testSuccess2 = false
          }
        })
    },
    queryDstMhaGtidCheckRes () {
      if (this.dstBuildParam.replicatorInitGtid == null || this.dstBuildParam.replicatorInitGtid === '') {
        alert('位点为空！')
        return
      }
      const that = this
      that.axios.get('/api/drc/v2/mha/gtid/checkResult?mha=' + this.dstBuildParam.mhaName +
        '&configGtid=' + this.dstBuildParam.replicatorInitGtid)
        .then(response => {
          if (response.data.status === 0) {
            this.gtidCheck.resVo = {
              mha: this.dstBuildParam.mhaName,
              legal: response.data.data.legal === true ? '合理位点' : 'binlog已经被purge',
              configGtid: this.dstBuildParam.replicatorInitGtid,
              purgedGtid: response.data.data.purgedGtid
            }
            this.gtidCheck.modal = true
          } else {
            alert('位点校验失败!')
          }
        })
    },
    getSrcDc () {
      this.axios.get('/api/drc/v2/mha/dc?mhaName=' + this.srcBuildParam.mhaName)
        .then(response => {
          this.srcDc = response.data.data
        })
    },
    getDstDc () {
      this.axios.get('/api/drc/v2/mha/dc?mhaName=' + this.dstBuildParam.mhaName)
        .then(response => {
          this.dstDc = response.data.data
        })
    },
    changeSrcMha () {
      this.$emit('srcMhaNameChanged', this.srcBuildParam.mhaName)
      this.getSrcMhaResources()
      this.getSrcMhaReplicatorsInUse()
      this.getSrcMhaAppliersInUse()
      this.getDstMhaAppliersInUse()
      this.getMhaDbAppliersInUse()
      this.getSrcDc()
    },
    changeDstMha () {
      this.$emit('dstMhaNameChanged', this.dstBuildParam.mhaName)
      this.getDstMhaResources()
      this.getDstMhaReplicatorsInUse()
      this.getDstMhaAppliersInUse()
      this.getSrcMhaAppliersInUse()
      this.getMhaDbAppliersInUse()
      this.getDstDc()
    },
    start () {
      this.$Loading.start()
    },
    finish () {
      this.$Loading.finish()
    },
    error () {
      this.$Loading.error()
    },
    changeModal (name) {
      this.$refs[name].validate((valid) => {
        if (!valid) {
          this.$Message.error('仍有必填项未填!')
        } else {
          this.drc.reviewModal = true
        }
      })
    },
    preCheckConfigure () {
      this.reviewModal = true
    },
    reviewConfigure () {
      this.drc.reviewModal = true
    },
    submitConfig () {
      const that = this
      this.axios.post('/api/drc/v2/config/', {
        srcBuildParam: this.srcBuildParam,
        dstBuildParam: this.dstBuildParam
      }).then(response => {
        console.log(response.data)
        that.result = response.data
        that.reviewModal = false
        that.resultModal = true
        this.refresh()
      })
    },
    getSrcDbReplications () {
      this.$router.push({
        path: '/dbTables',
        query: {
          srcMhaName: this.dstBuildParam.mhaName,
          dstMhaName: this.srcBuildParam.mhaName,
          srcDc: this.dstDc,
          dstDc: this.srcDc,
          order: true
        }
      })
    },
    getDstDbReplications () {
      this.$router.push({
        path: '/dbTables',
        query: {
          srcMhaName: this.srcBuildParam.mhaName,
          dstMhaName: this.dstBuildParam.mhaName,
          srcDc: this.srcDc,
          dstDc: this.dstDc,
          order: true
        }
      })
    },
    getSrcDbApplierReplications () {
      this.$router.push({
        path: '/dbAppliers',
        query: {
          srcMhaName: this.dstBuildParam.mhaName,
          dstMhaName: this.srcBuildParam.mhaName,
          srcDc: this.dstDc,
          dstDc: this.srcDc,
          order: true
        }
      })
    },
    getDstDbApplierReplications () {
      this.$router.push({
        path: '/dbAppliers',
        query: {
          srcMhaName: this.srcBuildParam.mhaName,
          dstMhaName: this.dstBuildParam.mhaName,
          srcDc: this.srcDc,
          dstDc: this.dstDc,
          order: true
        }
      })
    },
    handleChangeSize (val) {
      this.size = val
    },
    hasAppliers (dbApplierDtos) {
      for (const x of dbApplierDtos) {
        if (x.ips && x.ips.length > 0) {
          return true
        }
      }
      return false
    },
    showMhaApplierConfig (isSrc) {
      // return true
      if (isSrc) {
        return !this.hasAppliers(this.srcBuildData.dbApplierDtos)
      } else {
        return !this.hasAppliers(this.dstBuildData.dbApplierDtos)
      }
    },
    showDbMhaApplierConfig (isSrc) {
      if (isSrc) {
        return this.srcBuildData.dbApplierSwitch || this.hasAppliers(this.srcBuildData.dbApplierDtos)
      } else {
        return this.dstBuildData.dbApplierSwitch || this.hasAppliers(this.dstBuildData.dbApplierDtos)
      }
    },
    refresh () {
      this.getSrcMhaReplicatorsInUse()
      this.getDstMhaReplicatorsInUse()
      this.getSrcMhaAppliersInUse()
      this.getDstMhaAppliersInUse()
      this.getMhaDbAppliersInUse()
      this.getSrcDc()
      this.getDstDc()
    }
  },
  created () {
    this.getSrcMhaResources()
    this.getDstMhaResources()
    this.refresh()
  }
}
</script>
<style scoped>
.demo-split {
  height: 200px;
  border: 1px solid #dcdee2;
}

.demo-split-pane {
  padding: 10px;
}
</style>
