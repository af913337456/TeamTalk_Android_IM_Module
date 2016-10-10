package am.immodule.Core;


import am.immodule.Common.Logger;
import am.immodule.Common.SequenceNumberMaker;
import am.immodule.Common.SysConstant;

public class DefaultHeader extends Header {
    private Logger logger = Logger.getLogger(DefaultHeader.class);

    public DefaultHeader(int serviceId, int commandId) {
        setVersion((short) SysConstant.PROTOCOL_VERSION);
        setFlag((short) SysConstant.PROTOCOL_FLAG);
        setServiceId((short)serviceId);
        setCommandId((short)commandId);
        short seqNo = SequenceNumberMaker.getInstance().make();
        setSeqnum(seqNo);
        setReserved((short)SysConstant.PROTOCOL_RESERVED);

        logger.d("packet#construct Default Header -> serviceId:%d, commandId:%d, seqNo:%d", serviceId, commandId, seqNo);
    }
}
